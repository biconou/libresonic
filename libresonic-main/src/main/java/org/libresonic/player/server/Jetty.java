/**
 * Paquet de définition
 **/
package org.libresonic.player.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

import java.net.URL;
import java.security.ProtectionDomain;

/**
 * This Class creates a standalone jetty instance to run libresonic.
 *
 * Based on following article : http://blog.anvard.org/articles/2013/09/18/webmvc-server-3.html
 */
public class Jetty {

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);

        connector.setPort(8080);
        server.setConnectors(new Connector[] { connector});

        WebAppContext context = new WebAppContext();
        context.setServer(server);
        context.setContextPath("/libresonic");

        ProtectionDomain protectionDomain =
                Jetty.class.getProtectionDomain();
        URL location =
                protectionDomain.getCodeSource().getLocation();
        context.setWar(location.toExternalForm());

        server.setHandler(context);
        while (true) {
            try {
                server.start();
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            System.in.read();
            server.stop();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(100);
        }
    }
}
 
