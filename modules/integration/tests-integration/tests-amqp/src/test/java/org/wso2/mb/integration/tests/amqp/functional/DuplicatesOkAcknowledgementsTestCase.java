/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.mb.integration.tests.amqp.functional;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.mb.integration.common.clients.AndesClient;
import org.wso2.mb.integration.common.clients.configurations.AndesJMSConsumerClientConfiguration;
import org.wso2.mb.integration.common.clients.configurations.AndesJMSPublisherClientConfiguration;
import org.wso2.mb.integration.common.clients.exceptions.AndesClientException;
import org.wso2.mb.integration.common.clients.operations.utils.AndesClientConstants;
import org.wso2.mb.integration.common.clients.exceptions.AndesClientConfigurationException;
import org.wso2.mb.integration.common.clients.operations.utils.AndesClientUtils;
import org.wso2.mb.integration.common.clients.operations.utils.ExchangeType;
import org.wso2.mb.integration.common.clients.operations.utils.JMSAcknowledgeMode;
import org.wso2.mb.integration.common.utils.backend.MBIntegrationBaseTest;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

/**
 * This class includes test cases to test duplicate acknowledgements modes for queues
 */
public class DuplicatesOkAcknowledgementsTestCase extends MBIntegrationBaseTest {

    /**
     * Sending message count.
     */
    private static final long SEND_COUNT = 100L;

    /**
     * Expected message count.
     */
    private static final long EXPECTED_COUNT = SEND_COUNT;

    /**
     * Prepare environment for tests
     *
     * @throws XPathExpressionException
     */
    @BeforeClass
    public void prepare() throws XPathExpressionException {
        super.init(TestUserMode.SUPER_TENANT_USER);
        AndesClientUtils.sleepForInterval(1000);
    }

    /**
     * In this method we just test a sender and receiver with acknowledgements
     * 1. Create consumer client with duplicate acknowledge mode
     * 2. Publisher sends {@link #SEND_COUNT} messages.
     * 3. Consumer will receive {@link #EXPECTED_COUNT} or more messages.
     *
     * @throws AndesClientConfigurationException
     * @throws JMSException
     * @throws NamingException
     * @throws IOException
     * @throws AndesClientException
     */
    @Test(groups = {"wso2.mb", "queue"}, description = "Single queue send-receive test case with dup messages")
    public void duplicatesOkAcknowledgementsTest()
            throws AndesClientConfigurationException, JMSException, NamingException, IOException,
                   AndesClientException {

        // Creating a initial JMS consumer client configuration
        AndesJMSConsumerClientConfiguration consumerConfig = new AndesJMSConsumerClientConfiguration(ExchangeType.QUEUE, "dupOkAckTestQueue");
        consumerConfig.setMaximumMessagesToReceived(EXPECTED_COUNT);
        consumerConfig.setAcknowledgeMode(JMSAcknowledgeMode.DUPS_OK_ACKNOWLEDGE);
        consumerConfig.setPrintsPerMessageCount(EXPECTED_COUNT / 10L);

        AndesJMSPublisherClientConfiguration publisherConfig = new AndesJMSPublisherClientConfiguration(ExchangeType.QUEUE, "dupOkAckTestQueue");
        publisherConfig.setNumberOfMessagesToSend(SEND_COUNT);
        publisherConfig.setPrintsPerMessageCount(EXPECTED_COUNT / 10L);

        // Creating clients
        AndesClient consumerClient = new AndesClient(consumerConfig, true);
        consumerClient.startClient();

        AndesClient publisherClient = new AndesClient(publisherConfig, true);
        publisherClient.startClient();

        AndesClientUtils.sleepForInterval(5000);

        AndesClientUtils.waitForMessagesAndShutdown(consumerClient, AndesClientConstants.DEFAULT_RUN_TIME);

        // Evaluating
        Assert.assertEquals(publisherClient.getSentMessageCount(), SEND_COUNT, "Message send failed");
        Assert.assertTrue(consumerClient.getReceivedMessageCount() >= EXPECTED_COUNT, "The number of received messages should be equal or more than the amount sent");
    }
}
