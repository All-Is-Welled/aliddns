package com.jzb.aliddns;

import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AliddnsApplication {
    private static Log logging = LogFactory.getLog(AliddnsApplication.class);

    public static void main(String[] args) throws UnknownHostException {
        // logging.info(new ArrayList(Arrays.asList(args1)).toString());
        if (args.length < 4) {
            logging.error("no args");
            return;
        }
        logging.info("accessKeyId: " + args[0]);
        logging.info("accessKeySecret: ***************");
        logging.info("domainName: " + args[2]);
        logging.info("RRs: " + args[3]);
        logging.info("timeout: " + args[4]);

        String accessKeyId = args[0];
        String accessKeySecret = args[1];
        String domainName = args[2];
        String[] RRs = args[3].split(",");
        int timeout = Integer.parseInt(args[4]);

        DomainRecord.init(accessKeyId, accessKeySecret, domainName, RRs, timeout);

    }

}
