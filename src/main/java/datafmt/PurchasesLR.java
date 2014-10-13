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

import java.util.Arrays ;
import java.util.List ;

import javax.ws.rs.* ;

import jersey.repackaged.com.google.common.base.Objects ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.atlas.logging.FmtLog ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.* ;

@Path("/purchases")
public class PurchasesLR {
    static Logger log = LoggerFactory.getLogger(PurchasesLR.class) ;
    static String endpointURL = "http://lr-data-staging.epimorphics.com/landregistry/query" ;
    
//    @PathParam("town")
//    String town ;

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
    @Path("/towns/{town}")
    @Produces("application/json")
    public String getFixed(@PathParam("town") String town,
                           @QueryParam("roadname") String roadname,
                           @QueryParam("number")   String number,
                           @DefaultValue("false")
                              @QueryParam("inline")   String inline
                            ) {
        return request(town, roadname, number, Objects.equal(inline, "true")) ;
    }
        
    @GET
    @Path("/roads/{roadname}")
    @Produces("application/json")
    public String getFixed(
                           @PathParam("roadname") String roadname,
                           @QueryParam("number")   String number,
                           @DefaultValue("false")
                              @QueryParam("inline")   String inline
                            ) {
        return request("bristol", roadname, number, Objects.equal(inline, "true")) ;
    }

    
    private String request(String town, String roadname, String number, boolean inlineContext) { 
        try { 
            FmtLog.info(log, "town='%s' roadname='%s' number='%s'", town, roadname, number) ;
            
            if ( roadname == null )
                return error("No road name") ;
//            if ( roadname.length() == 0 )
//                error("zero length argument") ;
//            if ( roadname.matches("^\\s+$") )
//                error("Argument all whitespace") ;

            if ( ! present(town) )
                town = "bristol" ;
            if ( ! present(roadname) )
                error("No road name") ;

            
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
        } catch (RuntimeException ex) {
            ex.printStackTrace(System.err) ;
            throw ex ;
        }
    }

    private boolean present(String arg) {
        return arg != null && !arg.matches("^\\s*$") ;
    }
    
    private String error(String string) {
        return "{ \"error\": \""+string+"\" }\n" ;
    }

    String encode(String str) {
        char reserved[] = 
        {' ',
            '\n','\t',
            '!', '*', '"', '\'', '(', ')', ';', ':', '@', '&', 
            '=', '+', '$', ',', '/', '?', '%', '#', '[', ']'} ;

        char[] other = {'<', '>', '~', '.', '{', '}', '|', '\\', '-', '`', '_', '^'} ;        

        String y = StrUtils.encodeHex(str, '%', reserved) ;
        return y ;
    }
    
    
    // Outer template, inner template
    static String jsonOuter1 = StrUtils.strjoinNL
        ("{"
        ,"  \"timestamp\": \"{now}\" ,"
        ,"  \"data\":  ["
        ) ;
    static String jsonOuter2 = StrUtils.strjoinNL
        ("  ]"
        ,"}"
        ) ;

//    // Outer template, inner template
//    String jsonInner = StrUtils.strjoinNL
//        ("{"
//         ,"   \"a\" : \"{a}\""
//         ,"   \"b\" : \"{b}\""
//         ,"}"
//            ) ;

     String query(String textQuery) {
        String prefixes = StrUtils.strjoinNL("" 
        //,"prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
        //,"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
        //,"prefix owl: <http://www.w3.org/2002/07/owl#>"
        ,"prefix xsd: <http://www.w3.org/2001/XMLSchema#>"
        //,"prefix sr: <http://data.ordnancesurvey.co.uk/ontology/spatialrelations/>"
        //,"prefix lrhpi: <http://landregistry.data.gov.uk/def/hpi/>"
        //,"prefix lrppi: <http://landregistry.data.gov.uk/def/ppi/>"
        //,"prefix skos: <http://www.w3.org/2004/02/skos/core#>"
        //,"prefix lrcommon: <http://landregistry.data.gov.uk/def/common/>"
        ,"PREFIX  ppd:       <http://landregistry.data.gov.uk/def/ppi/>"
        ,"PREFIX  lrcommon:  <http://landregistry.data.gov.uk/def/common/>"
        ,"PREFIX  text:      <http://jena.apache.org/text#>"
        ) ;

        String queryString = StrUtils.strjoinNL(""
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
        ,"  { ?ppd_propertyAddress text:query ( '"+textQuery+"' ) ."
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
        return prefixes+"\n"+queryString ;
    }
    /*
     * "paon: ( 19 )  AND street: ( shipley )  AND town: ( bristol )"
     * "paon: ( 19 )  AND street: ( shipley AND road )" .
     * ( lrcommon:street "( shipley )" ) .
     */
}