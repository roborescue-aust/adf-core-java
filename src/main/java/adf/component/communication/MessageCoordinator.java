package adf.component.communication;

import java.util.ArrayList;
import java.util.List;

import adf.agent.communication.MessageManager;
import adf.agent.info.AgentInfo;
import adf.agent.info.ScenarioInfo;
import adf.agent.info.WorldInfo;

abstract public class MessageCoordinator {

  abstract public void coordinate(AgentInfo agentInfo, WorldInfo worldInfo, ScenarioInfo scenarioInfo,
      MessageManager messageManager, ArrayList<CommunicationMessage> sendMessageList,
      List<List<CommunicationMessage>> channelSendMessageList);
}
