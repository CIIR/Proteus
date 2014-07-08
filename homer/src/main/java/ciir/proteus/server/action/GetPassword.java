/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ciir.proteus.server.action;

import ciir.proteus.server.action.JSONHandler;
import ciir.proteus.system.ProteusSystem;
import org.lemurproject.galago.tupleflow.Parameters;

/**
 *
 * @author michaelz
 */
public class GetPassword implements JSONHandler {

    private final ProteusSystem system;

    public GetPassword(ProteusSystem proteus) {

        this.system = proteus;
    }

    @Override
    public Parameters handle(String method, String path, Parameters reqp) {

        Parameters os = new Parameters();
        os.put("OS", System.getProperty("os.name"));

        return os;
    }
}
