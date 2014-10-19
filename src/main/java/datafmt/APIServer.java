/*
 *  Copyright 2013, 2014 Andy Seaborne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package datafmt;

import java.io.IOException ;
import java.net.URI ;

import javax.ws.rs.core.UriBuilder ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Lib ;
import org.apache.jena.atlas.logging.LogCtl ;
import org.glassfish.grizzly.http.server.HttpHandlerRegistration ;
import org.glassfish.grizzly.http.server.HttpServer ;
import org.glassfish.grizzly.http.server.StaticHttpHandler ;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory ;
import org.glassfish.jersey.server.ResourceConfig ;
import org.slf4j.bridge.SLF4JBridgeHandler ;

public class APIServer {

    static { 
        LogCtl.setCmdLogging();
        // Optionally remove existing handlers attached to j.u.l root logger
        SLF4JBridgeHandler.removeHandlersForRootLogger();  // (since SLF4J 1.6.5)
        // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
        // the initialization phase of your application
        SLF4JBridgeHandler.install();
    }
    
    public static void main(String ...args) {
        int port = 9090 ;
        if ( args.length > 0 )
            port = Integer.parseInt(args[0]) ;
        
        grizzlyServer(port) ;
        for(;;) { Lib.sleep(100000); }
    }

    public static void grizzlyServer(int port) {       
        URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(port).build();
        ResourceConfig config = new ResourceConfig(PPD_API.class, HPI_API.class, API_Ctl.class);
        
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
        StaticHttpHandler pages = new StaticHttpHandler("pages") ;
        pages.setFileCacheEnabled(false);
        HttpHandlerRegistration registration1 = HttpHandlerRegistration.bulder()
            .urlPattern("*.html")
            .build() ; 
        HttpHandlerRegistration registration2 = HttpHandlerRegistration.bulder()
            .urlPattern("*.jsonld")
            .build() ; 
        HttpHandlerRegistration registration3 = HttpHandlerRegistration.bulder()
            .urlPattern("*.css")
            .build() ; 
        HttpHandlerRegistration registration4 = HttpHandlerRegistration.bulder()
            .urlPattern("*.js")
            .build() ; 
        server.getServerConfiguration().addHttpHandler(pages, registration1, registration2, registration3, registration4) ;
        
        
        try { server.start() ; }
        catch (IOException e) { IO.exception(e); }
    }

//    public static void jettyServer(int port) {    
//        URI baseUri = UriBuilder.fromUri("http://localhost/").port(9998).build();
//        ResourceConfig config = new ResourceConfig(ReportLR.class);
//        Server server = JettyHttpContainerFactory.createServer(baseUri, config);
//        // pages
//    }
}

