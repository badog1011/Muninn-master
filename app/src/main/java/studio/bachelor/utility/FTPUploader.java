package studio.bachelor.utility;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;


import java.io.IOException;
import java.io.InputStream;

/**
 * Created by BACHELOR on 2016/05/02.
 */
public class FTPUploader implements Runnable {
    public final String server;
    public final String username;
    public final String password;
    public int port = 21;
    public boolean error = false;
    public String folder = "/";

    InputStream inputStream;
    String filename;

    public FTPUploader(String server, String username, String password, int port) {
        this.server = server;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    public void setFile(String remote_filename, InputStream input_stream) {
        this.filename = remote_filename;
        this.inputStream = input_stream;
    }

    public void run() {
        FTPClient ftp_client = new FTPClient();
        try {
            ftp_client.connect(server, port);
            ftp_client.login(username, password);
            ftp_client.enterLocalActiveMode();
            ftp_client.setFileType(FTP.BINARY_FILE_TYPE);

            boolean done = ftp_client.storeFile(folder + filename, inputStream);
            if (done) {
                System.out.println("Uploaded.");
            }

        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            this.error = true;
        } finally {
            try {
                if (ftp_client.isConnected()) {
                    ftp_client.logout();
                    ftp_client.disconnect();
                }
            } catch (IOException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }
}