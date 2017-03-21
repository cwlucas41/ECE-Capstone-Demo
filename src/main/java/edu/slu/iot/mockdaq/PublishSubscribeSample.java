package edu.slu.iot.mockdaq;
/*
 * Copyright 2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;

import edu.slu.iot.IoTClient;
import edu.slu.iot.mockdaq.TestPublisher;
import edu.slu.iot.mockdaq.TestTopicListener;

/**
 * This is an example that uses {@link AWSIotMqttClient} to subscribe to a topic and
 * publish messages to it. Both blocking and non-blocking publishing are
 * demonstrated in this example.
 */
public class PublishSubscribeSample {

    public static void main(String args[]) throws InterruptedException, AWSIotException, AWSIotTimeoutException {

    	IoTClient client = new IoTClient("Certificate1/conf.txt");
    	
        client.subscribe(new TestTopicListener("test", AWSIotQos.QOS0));
        client.publish(new TestPublisher("test", AWSIotQos.QOS0, "demo"));
    }
}