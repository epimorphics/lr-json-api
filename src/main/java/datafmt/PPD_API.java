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

import static datafmt.DataWidgetLib.error ;
import static datafmt.DataWidgetLib.present ;

import java.util.Arrays ;
import java.util.List ;

import javax.ws.rs.* ;

import jersey.repackaged.com.google.common.base.Objects ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.FmtLog ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.* ;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP ;

@Path("/ppd")
public class PPD_API {
    static Logger log = LoggerFactory.getLogger(PPD_API.class) ;
    static String endpointURL = "http://lr-data-staging.epimorphics.com/landregistry/query" ;

//    @GET
//    @Path("/town/{town}")
//    @Produces("application/json")
//    public String getReport(@PathParam("town")      String town,
//                            @QueryParam("roadname") String roadname,
//                            @QueryParam("number")   String number,
//                            @DefaultValue("false")
//                                @QueryParam("inline")   String inline                            
//                            ) {
//        return request(town, roadname, number, Objects.equal(inline, "true")) ;
//    }
     
    @GET
    @Produces("application/json")
    public String getFixed(@DefaultValue("bristol") 
                              @QueryParam("town")     String town,
                           @QueryParam("roadname") String roadname,
                           @QueryParam("number")   String number,
                           @DefaultValue("false")
                              @QueryParam("inline")   String inline
                            ) {
        return request(town, roadname, number, Objects.equal(inline, "true")) ;
    }
        
    
    private String request(String town, String roadname, String number, boolean inlineContext) { 
        try { 
            FmtLog.info(log, "town='%s' roadname='%s' number='%s'", town, roadname, number) ;
            
            if ( roadname == null )
                return error("No road name",
                             "http://afs-1.epimorphics.com/api-doc.html",
                             "http://landregistry.data.gov.uk/ppd-documentation.html") ;
            if ( ! present(town) )
                town = "bristol" ;
            if ( ! present(roadname) )
                return error("No road name",
                             "http://afs-1.epimorphics.com/api-doc.html",
                             "http://landregistry.data.gov.uk/ppd-documentation.html") ;
            
            String ts = "town:( "+town+" ) AND street:( ROADNAME )" ;
            
            List<String> parts = Arrays.asList(roadname.split(" ", 0)) ;
            // Need to make safe
            String replacement = StrUtils.strjoin(" AND ", parts) ;
            ts = ts.replaceAll("ROADNAME", replacement) ;
            
            String ts2 = null ;
            if ( present(number) ) {
                ts2 = "paon:({number})" ;
                ts2 = ts2.replaceAll("\\{number\\}", number) ;
                ts = ts2+" AND "+ts ;
            }

            if ( log.isInfoEnabled() ) {
                FmtLog.info(log, "Text query string: \"%s\"", ts) ;
            }
            String queryString = query(ts) ;

            queryString = queryString.replaceAll("\\{roadname\\}", replacement) ;
            Query query = QueryFactory.create(queryString) ; 

            //System.out.println(queryString) ;
            try ( QueryExecution qExec = QueryExecutionFactory.sparqlService(endpointURL, query) ) {
                ResultSet resultSet = qExec.execSelect() ;
                return PPD_Results.resultsToJson(resultSet, inlineContext) ;
            }
        } catch (QueryExceptionHTTP ex) {
            if ( ex.getResponseMessage() == null )
                return error(ex.getResponseCode()+" "+ex.getMessage(),
                             "http://afs-1.epimorphics.com/api-doc.html",
                             "http://landregistry.data.gov.uk/ppd-documentation.html") ;
            else
                return error(ex.getResponseCode()+" "+ex.getResponseMessage(),
                             "http://afs-1.epimorphics.com/api-doc.html",
                             "http://landregistry.data.gov.uk/ppd-documentation.html") ;

        } catch (RuntimeException ex) {
            ex.printStackTrace(System.err) ;
            throw ex ;
        }
    }
    
    String query(String textQuery) {
        return  queryString.replace("TEXTQUERY", textQuery) ;
    }
    static String queryString = StrUtils.strjoinNL
        ("" 
         ,"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
         ,"PREFIX  ppd:       <http://landregistry.data.gov.uk/def/ppi/>"
         ,"PREFIX  lrcommon:  <http://landregistry.data.gov.uk/def/common/>"
         ,"PREFIX  text:      <http://jena.apache.org/text#>"
         ,""
         ,"SELECT"
         //," ?item"
         ,"  (?ppd_pricePaid AS ?PRICE)"
         ,"  (?ppd_transactionDate AS ?DATE)"
         ,"  (?item AS ?ppdTransaction)"
         ,"  ?ppd_transactionId"
         ,"  ?ppd_propertyAddress"
         ,"  (?ppd_propertyAddressSaon AS ?SAON)"
         ,"  (?ppd_propertyAddressPaon AS ?PAON)"
         ,"  (?ppd_propertyAddressStreet AS ?STREET)"
         ,"  (?ppd_propertyAddressLocality AS ?LOCALITY)"
         ,"  (?ppd_propertyAddressDistrict AS ?DISTICT)"
         ,"  (?ppd_propertyAddressTown AS ?TOWN)"
         ,"  (?ppd_propertyAddressCounty AS ?COUNTY)"
         ,"  (?ppd_propertyAddressPostcode AS ?POSTCODE)"  
         ,"WHERE"
         ,"  { ?ppd_propertyAddress text:query ( 'TEXTQUERY' ) ."
         ,"    ?item ppd:propertyAddress ?ppd_propertyAddress ."
         ,"    ?item ppd:pricePaid ?ppd_pricePaid ."
         ,"    ?item ppd:transactionDate ?ppd_transactionDate ."
         ,"    ?item ppd:transactionId ?ppd_transactionId"
         ,"    OPTIONAL { ?ppd_propertyAddress lrcommon:county ?ppd_propertyAddressCounty }"
         ,"    OPTIONAL { ?ppd_propertyAddress lrcommon:district ?ppd_propertyAddressDistrict }"
         ,"    OPTIONAL { ?ppd_propertyAddress lrcommon:locality ?ppd_propertyAddressLocality }"
         ,"    OPTIONAL { ?ppd_propertyAddress lrcommon:paon ?ppd_propertyAddressPaon }"
         ,"    OPTIONAL { ?ppd_propertyAddress lrcommon:postcode ?ppd_propertyAddressPostcode }"
         ,"    OPTIONAL { ?ppd_propertyAddress lrcommon:saon ?ppd_propertyAddressSaon }"
         ,"    OPTIONAL { ?ppd_propertyAddress lrcommon:street ?ppd_propertyAddressStreet }"
         ,"    OPTIONAL { ?ppd_propertyAddress lrcommon:town ?ppd_propertyAddressTown }"
         ,"  }"
         ,"LIMIT 1000"
            ) ;
}