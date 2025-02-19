package com.salawubabatunde.seafarerbiometric.services;


import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;
import javafx.application.Platform;
import javafx.concurrent.Task;

public class CaptureTask extends Task<Fid> {
    public interface CaptureListener {
        void onCaptureEvent(CaptureEvent event);
    }

    public static class CaptureEvent {
        public Reader.CaptureResult captureResult;
        public Reader.Status readerStatus;
        public UareUException exception;

        public CaptureEvent(Reader.CaptureResult cr, Reader.Status rs, UareUException ex) {
            this.captureResult = cr;
            this.readerStatus = rs;
            this.exception = ex;
        }
    }

    private final Reader reader;
    private final boolean isStreaming;
    private final Fid.Format format;
    private final Reader.ImageProcessing imageProcessing;
    private volatile boolean cancelRequested = false;
    private final CaptureListener listener;

    public CaptureTask(Reader reader, boolean isStreaming, Fid.Format format,
                       Reader.ImageProcessing imageProcessing, CaptureListener listener) {
        this.reader = reader;
        this.isStreaming = isStreaming;
        this.format = format;
        this.imageProcessing = imageProcessing;
        this.listener = listener;
    }

    @Override
    protected Fid call() throws Exception {
        if (isStreaming) {
            return stream();
        } else {
            return capture();
        }
    }

    private Fid capture() {
        try {
            boolean ready = waitForReaderReady();
            if (cancelRequested) {
                notifyListener(null, null, null, Reader.CaptureQuality.CANCELED);
                return null;
            }

            if (ready) {
                Reader.CaptureResult result = reader.Capture(format, imageProcessing,
                        reader.GetCapabilities().resolutions[0], -1);
                notifyListener(result, null, null, null);
                return result.image;
            }
        } catch (UareUException e) {
            notifyListener(null, null, e, null);
        }
        return null;
    }

    private Fid stream() {
        try {
            boolean ready = waitForReaderReady();
            if (ready) {
                reader.StartStreaming();
                while (!cancelRequested) {
                    Reader.CaptureResult result = reader.GetStreamImage(format, imageProcessing,
                            reader.GetCapabilities().resolutions[0]);
                    notifyListener(result, null, null, null);
                    if (result != null && result.image != null) {
                        return result.image;
                    }
                }
                reader.StopStreaming();
            }
        } catch (UareUException e) {
            notifyListener(null, null, e, null);
        }

        if (cancelRequested) {
            notifyListener(null, null, null, Reader.CaptureQuality.CANCELED);
        }
        return null;
    }

    private boolean waitForReaderReady() throws UareUException {
        while (!cancelRequested) {
            Reader.Status status = reader.GetStatus();
            if (status.status == Reader.ReaderStatus.BUSY) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else if (status.status == Reader.ReaderStatus.READY
                    || status.status == Reader.ReaderStatus.NEED_CALIBRATION) {
                return true;
            } else {
                notifyListener(null, status, null, null);
                break;
            }
        }
        return false;
    }

    private void notifyListener(Reader.CaptureResult cr, Reader.Status st,
                                UareUException ex, Reader.CaptureQuality cancelQuality) {
        if (cancelQuality != null) {
            cr = new Reader.CaptureResult();
            cr.quality = cancelQuality;
        }
        CaptureEvent event = new CaptureEvent(cr, st, ex);
        Platform.runLater(() -> {
            if (listener != null) {
                listener.onCaptureEvent(event);
            }
        });
    }

    public void cancelCapture() {
        cancelRequested = true;
        try {
            if (!isStreaming) {
                reader.CancelCapture();
            }
        } catch (UareUException ignored) {}
    }
}

