package edu.slu.iot.client;
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

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import edu.slu.iot.data.GsonSerializer;
import edu.slu.iot.data.Sample;

/**
 * This class extends {@link AWSIotTopic} to receive messages from a subscribed
 * topic.
 */
public class StrandListener extends AWSIotTopic {
	
	private StrandWindow sw;
	
    public StrandListener(String topic, AWSIotQos qos, StrandWindow passedWindow) {
        super(topic, qos);
        this.sw = passedWindow;
    }

    @Override
    public void onMessage(AWSIotMessage message) {
    	Sample sample = GsonSerializer.deserialize(message.getStringPayload(), Sample.class);
//        System.out.println(System.currentTimeMillis() + ": <<< " + sample.serialize());
        sw.writeLineToList(sample);
    }
}