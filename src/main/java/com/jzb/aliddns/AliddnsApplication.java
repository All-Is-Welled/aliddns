package com.jzb.aliddns;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AliddnsApplication {
    private static Log logging = LogFactory.getLog(AliddnsApplication.class);
    
    public static void main(String[] args) throws UnknownHostException {
//        logging.info(new ArrayList(Arrays.asList(args1)).toString());
        if (args.length < 4) {
            return;
        }
        
        String accessKeyId = args[0];
        String accessKeySecret = args[1];
        String domainName = args[2];
        String[] RRs = args[3].split(",");

        DomainRecord.init(accessKeyId, accessKeySecret,domainName, RRs);

    }

}
