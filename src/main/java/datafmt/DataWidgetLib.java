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

import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.atlas.lib.StrUtils ;

public class DataWidgetLib {

    public static boolean present(String arg) {
        return arg != null && !arg.matches("^\\s*$") ;
    }
    
    public static String error(String string, String docAPI, String docData) {
        JsonBuilder builder = new JsonBuilder() ;
        JsonValue v = builder
            .startObject()
            .key("error").value(string)
            .key("documentation_api").value(docAPI)
            .key("documentation_data").value(docData)
            .finishObject()
            .build() ;
        return v.toString()+"\n" ;
    }

    public static String encode(String str) {
        char reserved[] = 
        {' ',
            '\n','\t',
            '!', '*', '"', '\'', '(', ')', ';', ':', '@', '&', 
            '=', '+', '$', ',', '/', '?', '%', '#', '[', ']'} ;

        char[] other = {'<', '>', '~', '.', '{', '}', '|', '\\', '-', '`', '_', '^'} ;        

        String y = StrUtils.encodeHex(str, '%', reserved) ;
        return y ;
    }
}

