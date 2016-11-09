package ciir.proteus.server;

import ciir.proteus.server.action.*;
import ciir.proteus.system.ProteusSystem;
import ciir.proteus.users.error.DBError;
import ciir.proteus.users.http.*;
import ciir.proteus.util.logging.ClickLogHelper;
import ciir.proteus.util.HTTPUtil;
import ciir.proteus.util.logging.ClickLogData;
import ciir.proteus.util.logging.LogHelper;
import org.apache.logging.log4j.LogManager;
import org.lemurproject.galago.tupleflow.web.WebHandler;
import org.lemurproject.galago.utility.Parameters;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPRouter implements WebHandler {

    private static final Logger log = Logger.getLogger(HTTPRouter.class.getName());
    private static final org.apache.logging.log4j.Logger proteusLog = LogManager.getLogger("Proteus");
    private final JSONHandler debug;
    private final JSONHandler search;
    private final JSONHandler viewResource;
    private final JSONHandler getKinds;
    private final JSONHandler login;
    private final JSONHandler logout;
    private final JSONHandler register;
    private final JSONHandler getUsers;
    private final JSONHandler createCorpus;
    private final JSONHandler updateUserSettings;
    private final JSONHandler insertNote;
    private final JSONHandler updateNote;
    private final JSONHandler deleteNote;
    private final JSONHandler getNotesForResource;
    private final JSONHandler getNotesHistory;
    private final StaticContentHandler staticContent;
    private final JSONHandler getActivityLog;
    private final ProteusSystem proteus;
    private final JSONHandler getResourcesInCorpus;
    private final JSONHandler updateSubCorpora;
    private final JSONHandler resourceVote;

    public HTTPRouter(ProteusSystem proteus) {
        this.proteus = proteus;
        debug = new DebugHandler();

        staticContent = new StaticContentHandler(proteus.getConfig());

        search = new JSONSearch(proteus);
        viewResource = new ViewResource(proteus);
        getKinds = new GetKinds(proteus);
        login = new LoginUser(proteus);
        logout = new LogoutUser(proteus);
        register = new RegisterUser(proteus);
        getUsers = new GetUsers(proteus);
        createCorpus = new CreateCorpus(proteus);
        updateUserSettings = new UpdateUserSettings(proteus);
        insertNote = new InsertNote(proteus);
        updateNote = new UpdateNote(proteus);
        deleteNote = new DeleteNote(proteus);
        getNotesForResource = new GetNotesForResource(proteus);
        getNotesHistory = new GetNotesHistory(proteus);
        getActivityLog = new GetActivityLog(proteus);
        getResourcesInCorpus = new GetResourcesInCorpus(proteus);
        updateSubCorpora = new UpdateSubCorpora(proteus);
        resourceVote = new ResourceVote(proteus);
    }

    // handle http requests
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {

            String method = req.getMethod();
            String path = req.getPathInfo();

            Parameters reqp = HTTPUtil.fromHTTPRequest(req);

            final boolean GET = method.equals("GET");
            final boolean POST = method.equals("POST");
            final boolean PUT = method.equals("PUT");
            final boolean DELETE = method.equals("DELETE");

            JSONHandler handler;
            if ((GET || POST) && path.equals("/api/action")) {
                if (!reqp.isString("action")) {
                    throw new IllegalArgumentException("/api/action demands String key 'action'");
                }

                String action = reqp.getString("action");
                switch (action) {
                    case "search":
                    case "search-corpus":
                        handler = search;
                        break;
                    case "view":
                        handler = viewResource;
                        break;
                    default:
                        throw new IllegalArgumentException("/api/action doesn't know how to handle action=" + action);
                }
            } else if (GET && path.equals("/api/kinds")) {
                handler = getKinds;
            } else if (GET && path.equals("/api/users")) {
                handler = getUsers;
            } else if (POST && path.equals("/api/login")) {
                handler = login;
            } else if (POST && path.equals("/api/logout")) {
                handler = logout;
            } else if (POST && path.equals("/api/register")) {
                handler = register;
            } else if (POST && path.equals("/api/newcorpus")) {
                handler = createCorpus;
            } else if (POST && path.equals("/api/updatesettings")) {
                handler = updateUserSettings;
            }  else if (POST && path.equals("/api/updatesubcorpora")) {
                handler = updateSubCorpora;
            }else if (POST && path.equals("/api/resourcesincorpus")) {
                handler = getResourcesInCorpus;
            } else if (POST && path.equals("/api/resourcevote")) {
                handler = resourceVote;
            }else if (POST && path.equals("/api/notehistory")) {
                handler = getNotesHistory;
            } else if ((PUT) && path.startsWith("/store/annotations/upd")) {
                handleJSON(updateNote, method, path, reqp, resp, req, 303);
                return;
            }  else if ((DELETE) && path.startsWith("/store/annotations/del")) {
                handleJSON(deleteNote, method, path, reqp, resp, req, 303);
                return;
            }  else if ( POST  && path.equals("/store/annotations/ins")) {
                handleJSON(insertNote, method, path, reqp, resp, req, 303);
                return;
            }  else if ((GET || POST) && path.equals("/store/annotations/search")) {
                handleJSON(getNotesForResource, method, path, reqp, resp, req, 200);
                return;
            } else if (path.equals("/url")) {
                handleRedirect(reqp, req, resp);
                return;
            } else if (path.equals("/api/debug")) {
                handler = debug;
            }  else if ( POST  && path.equals("/api/activitylog")) {
                handler = getActivityLog;
            } else if (GET && !path.startsWith("/api/")) {
                staticContent.handle(path, reqp, resp);
                return;
            } else {
                resp.sendError(HTTPError.NotFound, "Not found");
                return;
            }
            handleJSON(handler, method, path, reqp, resp, req);
        } catch (IllegalArgumentException iae) {
            log.log(Level.INFO, "illegal argument received", iae);
            resp.sendError(HTTPError.BadRequest, iae.getMessage());
        } catch (Throwable th) {
            th.printStackTrace();
            resp.sendError(HTTPError.InternalError, th.getMessage());
        }
    }

    // the notes API requires that different status codes are returned, so we have a method that
    // accepts the correct code to return.
    private void handleJSON(JSONHandler which, String method, String path, Parameters reqp, HttpServletResponse resp, HttpServletRequest req) throws Exception {
        handleJSON(which, method, path, reqp, resp, req, 200);
    }

    // forward to JSON handler interface
    private void handleJSON(JSONHandler which, String method, String path, Parameters reqp, HttpServletResponse resp, HttpServletRequest req, Integer statusCode) throws Exception {
        resp.addHeader("Access-Control-Allow-Origin", "*");
        resp.addHeader("Content-Type", "application/json");

        try {
            Parameters response = which.handle(method, path, reqp, req);
            PrintWriter pw = resp.getWriter();
            pw.write(response.toString());
            pw.flush();
            pw.close();
            resp.setStatus(statusCode);
        } catch (HTTPError httpError) {
            // custom error type carries a HTTP status code
            log.log(Level.INFO, "http-error", httpError);
            resp.sendError(httpError.status, httpError.getMessage());
        } catch (IllegalArgumentException argex) {
            // Parameters.get failed
            log.log(Level.INFO, "illegal argument received", argex);
            resp.sendError(HTTPError.BadRequest, argex.getMessage());
        } catch (DBError dbError) {
            log.log(Level.WARNING, "database-error", dbError);
            resp.sendError(HTTPError.InternalError, dbError.getMessage());
        }

    }

    private void handleRedirect(Parameters reqp, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(reqp.getString("url"));

        // currently (7/2015) "user" isn't available for this action
        ClickLogData logData = new ClickLogData(ClickLogHelper.getID(reqp, req), reqp.get("user", ""));
        logData.setRank(reqp.getInt("rank"));
        logData.setUrl(reqp.getString("url"));
        LogHelper.log(logData, proteus);

    }

}
