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

import org.apache.jena.atlas.io.IndentedLineBuffer ;
import org.apache.jena.atlas.json.JSON ;
import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonValue ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.shared.uuid.JenaUUID ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeFunctions ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class HPI_Results {
    public static String resultsToJson(ResultSet resultSet, boolean inlineContext) {
        inlineContext = true ; // DEBUG
        JsonBuilder builder = new JsonBuilder() ;
        builder.startObject("outer") ;

        if ( ! inlineContext ) {
            builder.key("@context").value("http://afs-1.epimorphics.com/hpi-context.jsonld") ;
        } else {
            builder.key("@context") ;
            builder.startObject()
                .key("timestamp").value("http://landregistry.data.gov.uk/api/timestamp")
                .key("data").value("http://landregistry.data.gov.uk/api/data")
                // These are transitory in-message JSOn fields
                //.key("documentation_api").value("http://landregistry.data.gov.uk/api/documentation")
                //.key("documentation_data").value("http://landregistry.data.gov.uk/data/documentation")
                .key("hpi").value("http://landregistry.data.gov.uk/def/hpi/")
                ;
            builder.finishObject() ;
        }
        
        builder.key("@id").value(JenaUUID.generate().asURI()) ;        
        
        String nowStr = Utils.nowAsString() ;
        builder.key("timestamp").value(nowStr) ;
        builder.key("documentation_api").value("http://afs-1.epimorphics.com/api-doc.html") ;
        builder.key("documentation_data").value("http://landregistry.data.gov.uk/hpi-documentation.html") ;
        builder.key("data") ;
        builder.startArray() ;
        for ( ; resultSet.hasNext() ; ) {
            builder.startObject("ITEM") ;
            Binding row = resultSet.nextBinding() ;
            resultsToJson(builder, row) ;
            builder.finishObject("ITEM") ;
        }
        builder.finishArray() ;
        builder.finishObject("outer") ;
        JsonValue v = builder.build() ;
        try(IndentedLineBuffer out = new IndentedLineBuffer()) {
            JSON.write(out, v) ;
            out.println();
            return out.asString() ; 
        }
    }
    
    public static void resultsToJson(JsonBuilder builder, Binding row) {
        field(builder, row, "item", "@id") ;
        field(builder, row, "hpi_refRegion", "hpi:refRegion") ;
        field(builder, row, "hpi_refRegionName", "hpi:refRegionName") ;
        field(builder, row, "hpi_indices", "hpi:indices") ;
        field(builder, row, "hpi_indicesSA", "hpi:indicesSA") ;
        field(builder, row, "hpi_annualChange", "hpi:annualChange");
        field(builder, row, "hpi_monthlyChange", "hpi:monthlyChange") ;
        field(builder, row, "hpi_salesVolume", "hpi:salesVolume") ;
    }

    public static void field(JsonBuilder builder, Binding row, String varName, String fieldName) {
        Var var = Var.alloc(varName) ; 
        Node n = row.get(var) ;
        if ( n != null )
            builder.key(fieldName).value(NodeFunctions.str(n)) ;
    }
}

