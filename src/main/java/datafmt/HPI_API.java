/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package datafmt;

import static datafmt.DataWidgetLib.error ;
import static datafmt.DataWidgetLib.present ;

import java.util.Objects ;

import javax.ws.rs.* ;

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.FmtLog ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP ;

@Path("/hpi")
public class HPI_API {
    static Logger log = LoggerFactory.getLogger(HPI_API.class) ;
    static String endpointURL = "http://lr-data-staging.epimorphics.com/landregistry/query" ;
    //@PathParam("region")      String region ;
    
    @GET
    @Produces("application/json")
    public String getReportQS(@QueryParam("region")      String region ,
                              @DefaultValue("false") 
                                @QueryParam("inline")    String inline                            
        ) {
        return request(region, Objects.equals(inline, "true")) ;
    }
    
    
    @GET
    @Path("/{region}")
    @Produces("application/json")
    public String getReport(@PathParam("region")      String region ,
                            @DefaultValue("false") 
                            @QueryParam("inline")    String inline                            
        ) {
        return request(region, Objects.equals(inline, "true")) ;
    }
    
    private String request(String region, boolean inlineContext) {
        // city-of-bristol
        try { 
            FmtLog.info(log, "region='%s'", region) ;
            if ( ! present(region) )
                return error("No region name", 
                             "http://afs-1.epimorphics.com/api-doc.html",
                             "http://landregistry.data.gov.uk/hpi-documentation.html") ;
            
            String queryString = query(region) ;
            Query query = QueryFactory.create(queryString) ; 
            try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(endpointURL, query) ) {
                ResultSet resultSet = qExec.execSelect() ;
                return HPI_Results.resultsToJson(resultSet, inlineContext) ;
            }
            
        } catch (QueryExceptionHTTP ex) {
            return error(ex.getResponseMessage(),
                         "http://afs-1.epimorphics.com/api-doc.html",
                         "http://landregistry.data.gov.uk/hpi-documentation.html") ;
        } catch (RuntimeException ex) {
            ex.printStackTrace(System.err) ;
            throw ex ;
        }
    }
    
    private String query(String region) {
        return queryString.replace("REGION", region) ;
    }

    static String queryString = StrUtils.strjoinNL
        ("PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>"
         ,"PREFIX  hpi:  <http://landregistry.data.gov.uk/def/hpi/>"
         ,"PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>"
         ,""
         ,"SELECT"
         ,"    ?item"
         ,"    ?hpi_refPeriod"
         ,"    ?hpi_refRegion"
         ,"    ?hpi_indices"
         ,"    ?hpi_indicesSA"
         ,"    ?hpi_refRegionName"
         ,"    ?hpi_annualChange"
         ,"    ?hpi_averagePricesDetachedSASM"
         ,"    ?hpi_averagePricesFlatMaisonetteSASM"
         ,"    ?hpi_averagePricesSA"
         ,"    ?hpi_averagePricesSASM"
         ,"    ?hpi_averagePricesSemiDetachedSASM"
         ,"    ?hpi_averagePricesTerracedSASM"
         ,"    ?hpi_indicesSASM"
         ,"    ?hpi_monthlyChange"
         ,"    ?hpi_salesVolume"
         ,"WHERE"
         ,"  { ?item hpi:refPeriod ?hpi_refPeriod ."
         ,"    ?item hpi:refRegion <http://landregistry.data.gov.uk/id/region/REGION> ."
         ,"   BIND(<http://landregistry.data.gov.uk/id/region/REGION> AS ?hpi_refRegion)"
         ,"    ?item hpi:refRegionName ?hpi_refRegionName ."
         ,"    ?item hpi:indices ?hpi_indices ."
         ,"    ?item hpi:indicesSA ?hpi_indicesSA ."
         ,"    OPTIONAL { ?item hpi:annualChange ?hpi_annualChange }"
         ,"    OPTIONAL { ?item hpi:averagePricesDetachedSASM ?hpi_averagePricesDetachedSASM }"
         ,"    OPTIONAL { ?item hpi:averagePricesFlatMaisonetteSASM ?hpi_averagePricesFlatMaisonetteSASM }"
         ,"    OPTIONAL { ?item hpi:averagePricesSA ?hpi_averagePricesSA }"
         ,"    OPTIONAL { ?item hpi:averagePricesSASM ?hpi_averagePricesSASM }"
         ,"    OPTIONAL { ?item hpi:averagePricesSemiDetachedSASM ?hpi_averagePricesSemiDetachedSASM }"
         ,"    OPTIONAL { ?item hpi:averagePricesTerracedSASM ?hpi_averagePricesTerracedSASM }"
         ,"    OPTIONAL { ?item hpi:indicesSASM ?hpi_indicesSASM }"
         ,"    OPTIONAL { ?item hpi:monthlyChange ?hpi_monthlyChange }"
         ,"    OPTIONAL { ?item hpi:salesVolume ?hpi_salesVolume }"
         ,"  }"
         ,"ORDER BY DESC(?hpi_refPeriod)"
         ,"LIMIT 12"
            ) ;
}

