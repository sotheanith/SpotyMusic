package mesh.dfs;

import net.Constants;
import net.common.DeferredStreamJsonGenerator;
import net.reqres.Socketplexer;

import java.io.*;

public class ReadBlockRequestHandler implements Runnable {

    private final Socketplexer socketplexer;

    private final BlockDescriptor block;

    private DFS dfs;

    public ReadBlockRequestHandler(Socketplexer socketplexer, BlockDescriptor block, DFS dfs) {
        this.socketplexer = socketplexer;
        this.block = block;
        this.dfs = dfs;
    }

    @Override
    public void run() {
        File f = this.block.getFile();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = new BufferedInputStream(new FileInputStream(f));

            this.dfs.executor.submit(new DeferredStreamJsonGenerator(this.socketplexer.openOutputChannel(1), true, (gen) -> {
                gen.writeStartObject();
                gen.writeStringField(Constants.REQUEST_TYPE_PROPERTY, DFS.RESPONSE_READ_BLOCK);
                gen.writeStringField(Constants.PROPERTY_RESPONSE_STATUS, Constants.RESPONSE_STATUS_OK);
                gen.writeEndObject();
            }));

            out = this.socketplexer.openOutputChannel(2);

            byte[] trx = new byte[1024 * 8];
            int read = 0;
            while ((read = in.read(trx, 0, trx.length)) != -1) {
                out.write(trx, 0, read);
            }

            in.close();
            out.close();

        } catch (FileNotFoundException e) {
            this.dfs.executor.submit(new DeferredStreamJsonGenerator(this.socketplexer.openOutputChannel(1), true, (gen) -> {
                gen.writeStartObject();
                gen.writeStringField(Constants.REQUEST_TYPE_PROPERTY, DFS.RESPONSE_READ_BLOCK);
                gen.writeStringField(Constants.PROPERTY_RESPONSE_STATUS, Constants.RESPONSE_STATUS_NOT_FOUND);
                gen.writeEndObject();
            }));
            System.err.println("[ReadBlockRequestHandler][run] Attempt to read nonexistent block! " + block.getBlockName());

        } catch (IOException e) {
            System.err.println("[ReadBlockRequestHandler][run] IOException while reading block " + block.getBlockName());
            e.printStackTrace();

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    System.err.println("[ReadBlockRequestHandler][run] Unable to close inputstream after IOException!");
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    System.err.println("[ReadBlockRequestHandler][run] Unable to close response stream after IOException!");
                }
            }
        }

        System.out.println("[ReadBlockRequestHandler][run] Successfully read block " + block.getBlockName());
    }
}