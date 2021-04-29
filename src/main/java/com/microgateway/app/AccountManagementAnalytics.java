/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.microgateway.app;

import com.solacesystems.jcsmp.*;

import java.io.IOException;

public class AccountManagementAnalytics {

    public void run(String... args) throws JCSMPException {
        System.out.println("VerifyExternalAccount processor initializing...");

        final String hostName = "tcp://localhost:55555";
        final String vpnName = "default";
        final String username = "default";
        final String password = "default";

        // Create a JCSMP Session
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, hostName);     // host:port
        properties.setProperty(JCSMPProperties.VPN_NAME,  vpnName); // message-vpn
        properties.setProperty(JCSMPProperties.USERNAME, username); // client-username
        properties.setProperty(JCSMPProperties.PASSWORD, password); // client-password
        properties.setProperty(JCSMPProperties.IGNORE_DUPLICATE_SUBSCRIPTION_ERROR, true); // Make sure that the session is tolerant of the subscription already existing on the queue.

        final JCSMPSession session = JCSMPFactory.onlyInstance().createSession(properties);
        session.connect();

        final Topic requestTopic = JCSMPFactory.onlyInstance().createTopic("*/ave/>");
        final Topic errorTopic = JCSMPFactory.onlyInstance().createTopic("ave/>");
        final Topic snoopTopic = JCSMPFactory.onlyInstance().createTopic("#P2P/*/#rest*/>");

        /** Anonymous inner-class for consuming**/
        final XMLMessageConsumer cons = session.getMessageConsumer(new XMLMessageListener() {
            @Override
            public void onReceive(BytesXMLMessage msg) {
                System.out.println("Received message on topic " + msg.getDestination().toString());
            }

            public void onException(JCSMPException e) {
                System.out.printf("Consumer received exception: %s%n", e);
            }
        });

        session.addSubscription(requestTopic);
        session.addSubscription(errorTopic);
        session.addSubscription(snoopTopic);
        cons.start();

        // Consume-only session is now hooked up and running!
        System.out.println("Listening for messages on topic " + requestTopic + " and " + snoopTopic + " and " + errorTopic + " ... Press enter to exit");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Close consumer
        cons.close();
        System.out.println("Exiting.");
        session.closeSession();

    }

    public static void main(String... args) throws JCSMPException {
        AccountManagementAnalytics replier = new AccountManagementAnalytics();
        replier.run(args);
    }
}