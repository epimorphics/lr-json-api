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

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.riot.ResultSetMgr ;
import org.apache.jena.riot.resultset.ResultSetLang ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.query.ResultSetRewindable ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeFunctions ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderResultSet ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class DataFmtMain {

    public static void main(String[] args) {
        String request = "" ;
        
        Var var_a = Var.alloc("a") ;
        Var var_b = Var.alloc("b") ;
        String x = StrUtils.strjoinNL
            ( "(resultset (?a ?b)"
             ,"  (row (?a 1) (?b 'foo'))"
             ,"  (row (?a 2) (?b 'bar'))"
             ,")"
                ) ;
        
        // Outer template, inner template
        String jsonOuter1 = StrUtils.strjoinNL
            ("{"
             ,"   \"timestamp\": \"{now}\""
             ,"   \"data\":  ["
                ) ;
        String jsonOuter2 = StrUtils.strjoinNL
            ( "   ]"
             ,"}"
                ) ;

        // Outer template, inner template
        String jsonInner = StrUtils.strjoinNL
            ("{"
             ,"   \"a\" : \"{a}\""
             ,"   \"b\" : \"{b}\""
             ,"}"
                ) ;

        ResultSetRewindable rs = ResultSetFactory.makeRewindable(BuilderResultSet.build(SSE.parse(x))) ;
        ResultSetFormatter.out(rs);
        rs.reset();
        ResultSetMgr.write(System.out, rs, ResultSetLang.SPARQLResultSetCSV) ;
        rs.reset();
        System.out.println() ;
        IndentedWriter out = IndentedWriter.stdout ;
        
        String nowStr = Utils.nowAsString() ;
        String y = jsonOuter1 ;
        y = y.replace("{now}", nowStr) ;
        out.print(y) ;
        out.incIndent();
        out.incIndent();
        boolean first = true ;
        for ( ; rs.hasNext() ; ) {
            Binding b = rs.nextBinding() ;
            Node val_a = b.get(var_a) ;
            Node val_b = b.get(var_b) ;
            String $ = jsonInner ;
            $ = $.replace("{a}", NodeFunctions.str(val_a)) ;
            $ = $.replace("{b}", NodeFunctions.str(val_b)) ;
            if ( ! first )
                out.print(",") ;
            first = false ;
            out.print($) ;
        }
        out.println() ;
        out.decIndent();
        out.decIndent();
        y = jsonOuter2 ;
        y = y.replace("{now}", nowStr) ;
        out.println(y) ;
        out.flush() ;
        System.out.println() ;
        System.out.println("DONE") ;
        
    }

}

