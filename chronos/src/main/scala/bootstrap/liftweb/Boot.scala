/* {{{
 *  Copyright 2012 Franz Bettag <franz@bett.ag>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/// }}}

package bootstrap.liftweb

import net.liftweb.common._
import net.liftweb.mapper._
import net.liftweb.http._
import net.liftweb.util._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import ag.bett.lift.bhtml._
import ciir.models.StopWordList
import java.io.{DataInputStream, FileInputStream}




class Boot {
	def boot {

		StopWordList.reloadStopWordFromInputStream(new DataInputStream(new FileInputStream("stopwords")), true)
		// Where to search snippet
		LiftRules.addToPackages("ag.bett.lift.bootstrap")
		LiftRules.addToPackages("code")
		LiftRules.explicitlyParsedSuffixes = Set("htm", "html", "shtml")

		
		// Build SiteMap
		def sitemap = SiteMap(
			Menu.i("Login") / "index",
			Menu.i("Home") / "home"
		)

		// Using [[ag.bett.lift.bhtml.NginxSendfileResponse]]
		LiftRules.dispatch.append {
			// File Download
			case Req("pdf" :: "download" :: name :: Nil, _, GetRequest) =>
				() => Full(NginxSendfileResponse("/sendfile/pdf", name, "application/pdf", List(("Content-Disposition" -> "attachment"))))
			// Inline display
			case Req("pdf" :: "display" :: name :: Nil, _, GetRequest) =>
				() => Full(NginxSendfileResponse("/sendfile/pdf", name, "application/pdf", Nil))
		}

		// set the sitemap.	Note if you don't want access control for
		// each page, just comment this line out.
		//LiftRules.setSiteMapFunc(() => User.sitemapMutator(sitemap))

		// Use jQuery 1.4
		LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

		//Show the spinny image when an Ajax call starts
		LiftRules.ajaxStart =
			Full(() => LiftRules.jsArtifacts.show("ajax-loader3").cmd)

		// Make the spinny image go away when it ends
		LiftRules.ajaxEnd =
			Full(() => LiftRules.jsArtifacts.hide("ajax-loader3").cmd)

		// Force the request to be UTF-8
		LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

		// 30 seconds for querying, this is horrible.
		LiftRules.ajaxPostTimeout = 30000
		LiftRules.ajaxRetryCount = Full(0)
		// Make file uploads to be written onto disk instead of ram
		LiftRules.handleMimeFile = OnDiskFileParamHolder.apply

		// Use HTML5 for rendering
		LiftRules.htmlProperties.default.set((r: Req) =>
			new Html5Properties(r.userAgent))


		// Make a transaction span the whole HTTP request
	//	S.addAround(DB.buildLoanWrapper)
	}
}
