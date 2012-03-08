/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package code
package snippet

import java.io.File
import java.io.FileInputStream
import net.liftweb.common.Logger
import net.liftweb.http.GetRequest
import net.liftweb.http.Req
import net.liftweb.http.SHtml
import net.liftweb.http.StreamingResponse
import net.liftweb.http.js.JsCmds
import net.liftweb.http.rest.RestHelper


object LogServer extends RestHelper with Logger {
    
    
    def render = {
        <p><a href="/log/all">Full Log</a></p>
    }
    
    def logLink(href: String, text: String, logText: String) = {
      SHtml.a(() => {info(logText); JsCmds.RedirectTo(href)}, <span>{text}</span>)
    }
   
    serve { 
        case Req("log" :: "all" :: _, _, GetRequest) => {
            // InMemoryResponse(bytes, List("Content-Type" -> "text/csv"), Nil, 200) 
            val log = new File("proteus.log")
            val log_stream = new FileInputStream(log)
            val log_size = log.length// must compute or predetermine this.
            StreamingResponse(log_stream,
                                  () => { log_stream.close },
                                  log_size,
                                  ("Content-Type" -> "text/plain") ::
                                  ("Content-disposition" -> ("attachment; filename=" + log.toString)) 
                                  :: Nil, // tack on the date as well?
                                  Nil, 
                                  200)
        }

        case Req("log" :: "annotations" :: _, _, GetRequest) => {
            // InMemoryResponse(bytes, List("Content-Type" -> "text/csv"), Nil, 200)
            val log = new File("annotations.log")
            val log_stream = new FileInputStream(log)
            val log_size = log.length// must compute or predetermine this.
            StreamingResponse(log_stream,
                                  () => { log_stream.close },
                                  log_size,
                                  ("Content-Type" -> "text/plain") ::
                                  ("Content-disposition" -> ("attachment; filename=" + log.toString))
                                  :: Nil, // tack on the date as well?
                                  Nil,
                                  200)
        }
        
    } 


}
