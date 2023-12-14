import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.*;
import org.eclipse.jetty.util.thread.*;
import org.eclipse.jetty.http.*;
import org.eclipse.jetty.server.handler.*;

public class MyServer {
    
    public static void main(String[] args) { 
        try {
            // Spin up the server
            Server server = new Server(40106);
            ResourceHandler rHandler = new ResourceHandler();
            rHandler.setResourceBase(".");
            // Set up default redirect behavior if someone goes to localhost:40106
            rHandler.setWelcomeFiles(new String[] {"login.html"});
            ContextHandler cHandler = new ContextHandler("/");
            cHandler.setHandler(rHandler);
            ServletContextHandler sHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);

            // Install the servlet(s)
            ServletHolder servletHolder = new ServletHolder(new ExamServlet());
            sHandler.addServlet(servletHolder, "/examservlet");
            // Additional servlets can be added similarly

            ContextHandlerCollection contexts = new ContextHandlerCollection();
            contexts.setHandlers(new Handler[] {cHandler, sHandler});
            server.setHandler(contexts);

            // Start the server
            server.start();
            System.out.println("Web server started, listening for browser connections on port 40106...");
            server.join();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
