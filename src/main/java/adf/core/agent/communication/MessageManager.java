package adf.core.agent.communication;

import adf.core.agent.communication.standard.bundle.StandardMessageBundle;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.component.communication.ChannelSubscriber;
import adf.core.component.communication.CommunicationMessage;
import adf.core.component.communication.MessageBundle;
import adf.core.component.communication.MessageCoordinator;
import adf.core.launcher.ConsoleOutput;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class MessageManager {

    private int standardMessageClassCount;
    private int customMessageClassCount;
    private HashMap<Integer,
            Class<? extends CommunicationMessage>> messageClassMap;
    private HashMap<Class<? extends CommunicationMessage>,
            Integer> messageClassIDMap;
    private ArrayList<CommunicationMessage> sendMessageList;
    private List<List<CommunicationMessage>> channelSendMessageList;
    private List<CommunicationMessage> receivedMessageList;
    private int heardAgentHelpCount;
    private MessageCoordinator messageCoordinator;

    private Set<String> checkDuplicationCache;

    private ChannelSubscriber channelSubscriber;
    private int[] subscribedChannels;
    private boolean isSubscribed;

    public MessageManager() {
        this.standardMessageClassCount = 1; // 00001
        this.customMessageClassCount = 16; // 10000
        this.messageClassMap = new HashMap<>(32);
        this.messageClassIDMap = new HashMap<>(32);
        this.sendMessageList = new ArrayList<>();
        this.channelSendMessageList = new ArrayList<>();
        this.checkDuplicationCache = new HashSet<>();
        this.receivedMessageList = new ArrayList<>();
        this.heardAgentHelpCount = 0;

        this.messageCoordinator = null;

        channelSubscriber = null;
        subscribedChannels = new int[1];
        // by default subscribe to channel 1
        subscribedChannels[0] = 1;
        isSubscribed = false;
    }


    public void subscribeToChannels(int[] channels) {
        subscribedChannels = channels;
        isSubscribed = false;
    }


    public int[] getChannels() {
        return subscribedChannels;
    }


    public boolean getIsSubscribed() {
        return isSubscribed;
    }


    public void setIsSubscribed(boolean subscribed) {
        isSubscribed = subscribed;
    }


    public void setMessageCoordinator(MessageCoordinator mc) {
        this.messageCoordinator = mc;
    }


    public void setChannelSubscriber(ChannelSubscriber cs) {
        channelSubscriber = cs;
    }


    public void subscribe(AgentInfo agentInfo, WorldInfo worldInfo,
                          ScenarioInfo scenarioInfo) {
        if (channelSubscriber != null) {
            channelSubscriber.subscribe(agentInfo, worldInfo, scenarioInfo, this);
        }
    }


    public boolean registerMessageClass(int index,
                                        @Nonnull Class<? extends CommunicationMessage> messageClass) {
        if (index > 31) {
            throw new IllegalArgumentException("index maximum is 31");
        }

        if (messageClassMap.containsKey(index)) {
            ConsoleOutput.out(ConsoleOutput.State.WARN,
                    "index(" + index + ") is already registered/" + messageClass.getName()
                            + " is ignored");
            return false;
        }

        messageClassMap.put(index, messageClass);
        messageClassIDMap.put(messageClass, index);

        return true;
    }


    public void registerMessageBundle(@Nonnull MessageBundle messageBundle) {
        if (messageBundle == null) {
            return;
        }

        for (Class<? extends CommunicationMessage> messageClass : messageBundle
                .getMessageClassList()) {
            this.registerMessageClass(
                    (messageBundle.getClass().equals(StandardMessageBundle.class)
                            ? standardMessageClassCount++
                            : customMessageClassCount++),
                    messageClass);
        }
    }


    @Nullable
    public Class<? extends CommunicationMessage> getMessageClass(int index) {
        if (!messageClassMap.containsKey(index)) {
            return null;
        }

        return messageClassMap.get(index);
    }


    public int getMessageClassIndex(@Nonnull CommunicationMessage message) {
        if (!messageClassMap.containsValue(message.getClass())) {
            throw new IllegalArgumentException(
                    message.getClass().getName() + " is not registered with the manager");
        }

        return messageClassIDMap.get(message.getClass());
    }


    public void addMessage(@Nonnull CommunicationMessage message) {
        this.addMessage(message, true);
    }


    public void addMessage(@Nonnull CommunicationMessage message,
                           boolean checkDuplication) {
        if (message == null) {
            return;
        }

        String checkKey = message.getCheckKey();
        if (checkDuplication && !this.checkDuplicationCache.contains(checkKey)) {
            this.sendMessageList.add(message);
            this.checkDuplicationCache.add(checkKey);
        } else {
            this.sendMessageList.add(message);
            this.checkDuplicationCache.add(checkKey);
        }
    }


    @Nonnull
    public List<List<CommunicationMessage>> getSendMessageList() {
        return this.channelSendMessageList;
    }

    /**
     * 添加收到的消息
     * @param message 消息
     * @author <a href="https://roozen.top">Roozen</a>
     */
    public void addReceivedMessage(@Nonnull CommunicationMessage message) {
        receivedMessageList.add(message);
    }


    /**
     * 获取收到的消息
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Nonnull
    public List<CommunicationMessage> getReceivedMessageList() {
        return this.receivedMessageList;
    }

    /**
     * 根据消息类型获取收到的消息
     * @param messageClasses 消息类型
     * @return 收到的消息中类型为传入的消息类型及其子类类型的消息
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @SafeVarargs
    @Nonnull
    public final List<CommunicationMessage> getReceivedMessageList(
            Class<? extends CommunicationMessage>... messageClasses) {
        List<CommunicationMessage> resultList = new ArrayList<>();
        for (CommunicationMessage message : this.receivedMessageList) {
            for (Class<? extends CommunicationMessage> messageClass : messageClasses) {
                if (messageClass.isAssignableFrom(message.getClass())) {
                    resultList.add(message);
                }
            }
        }
        return resultList;
    }


    public void coordinateMessages(AgentInfo agentInfo, WorldInfo worldInfo,
                                   ScenarioInfo scenarioInfo) {
        // 为每个频道（包括语音通信）创建消息列表
        // 频道
        this.channelSendMessageList = new ArrayList<List<CommunicationMessage>>(
                scenarioInfo.getCommsChannelsCount());
        for (int i = 0; i < scenarioInfo.getCommsChannelsCount(); i++) {
            this.channelSendMessageList.add(new ArrayList<CommunicationMessage>());
        }

        if (messageCoordinator != null) {
            messageCoordinator.coordinate(agentInfo, worldInfo, scenarioInfo, this,
                    this.sendMessageList, this.channelSendMessageList);
        }
    }


    public void addHeardAgentHelpCount() {
        this.heardAgentHelpCount++;
    }


    public int getHeardAgentHelpCount() {
        return this.heardAgentHelpCount;
    }


    public void refresh() {
        this.sendMessageList.clear();
        this.checkDuplicationCache.clear();
        this.receivedMessageList.clear();
        this.heardAgentHelpCount = 0;
    }
}