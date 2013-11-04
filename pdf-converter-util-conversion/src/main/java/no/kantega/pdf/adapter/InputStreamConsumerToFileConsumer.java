package no.kantega.pdf.adapter;

import com.google.common.base.Objects;
import no.kantega.pdf.api.IFileConsumer;
import no.kantega.pdf.api.IInputStreamConsumer;
import no.kantega.pdf.throwables.FileSystemInteractionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

class InputStreamConsumerToFileConsumer implements IFileConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(InputStreamConsumerToFileConsumer.class);

    private final IInputStreamConsumer inputStreamConsumer;

    public InputStreamConsumerToFileConsumer(IInputStreamConsumer inputStreamConsumer) {
        this.inputStreamConsumer = inputStreamConsumer;
    }

    @Override
    public void onComplete(File file) {
        try {
            inputStreamConsumer.onComplete(new DeleteFileOnCloseInputStream(file));
        } catch (IOException e) {
            throw new FileSystemInteractionException(String.format("Could not process file: %s", file), e);
        }
    }

    @Override
    public void onCancel(File file) {
        try {
            inputStreamConsumer.onCancel();
        } finally {
            tryDelete(file);
        }
    }

    @Override
    public void onException(File file, Exception e) {
        try {
            inputStreamConsumer.onException(e);
        } finally {
            tryDelete(file);
        }
    }

    private void tryDelete(File file) {
        if (file.exists() && !file.delete()) {
            LOGGER.warn("Could not delete target file {} after unsuccessful conversion", file);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(InputStreamConsumerToFileConsumer.class)
                .add("inputStreamConsumer", inputStreamConsumer)
                .toString();
    }
}