package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.Calendar;
import java.util.Scanner;

public class Chat {

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("ip-da-instancia-da-aws"); // Alterar
    factory.setUsername("admin"); // Alterar
    factory.setPassword("admin"); // Alterar
    factory.setVirtualHost("/");

    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    System.out.print("user: ");
    Scanner sc = new Scanner(System.in);
    String user = sc.nextLine().substring(6);
    
    // Criar fila para o usuário caso ela ainda não exista
    channel.queueDeclare(user, false,   false,     false,       null);
    
    // Ouve a fila do usuário
    Consumer consumer = new DefaultConsumer(channel) {
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] buffer) throws IOException {
        // Realizar tratamento do objeto da mensagem como buffer de bytes
        FileInputStream fis = new FileInputStream("Message");
        fis.read(buffer);
        fis.close();
        MessageProto.Message contatoAluno = MessageProto.Message.parseFrom(buffer);

        String SenderName  = contatoAluno.getSenderName();
        String Message     = contatoAluno.getMessage();
        String Date        = contatoAluno.getDate();
        String Hour        = contatoAluno.getHour();
        String Minute      = contatoAluno.getMinute();
        String Group       = contatoAluno.getGroup();

        // Realizar tratamento do objeto da mensagem como buffer de bytes
        if(Group.equals("")){
          System.out.println("("+Date+" às "+Hour+":"+Minute+") "+SenderName+" diz: "+Message);
        }else{
          // mensagem exemplo: (21/09/2018 às 21:50) joaosantos#amigos diz: Olá, amigos!!!
          System.out.println("("+Date+" às "+Hour+":"+Minute+") "+SenderName+"#"+Group+" diz: "+Message);
        }
      }
    };
    //(queue-name, autoAck, consumer);    
    channel.basicConsume(user, true,    consumer);

    String currentChatCommand = ">>";
    String currentUser = "";
    // Chat ativo
    while (true) {
      // Enviar mensagem para a fila do usuário
      System.out.print(currentChatCommand);
      String message = sc.nextLine();
      boolean isGroup = false;

      // Enviar mensagem para a fila do grupo
      if(isGroup){
        String groupName = currentChatCommand.replaceAll("(?<=#)[^>]+(?=>>)","");
        Date  date= new Date(System.currentTimeMillis());

        Calendar.getInstance().setTime(date);
        MessageProto.Message.Builder builderMessage = MessageProto.Message.newBuilder();
        builderMessage.setSenderName(user);
        builderMessage.setMessage(message);
        builderMessage.setDate(date.toString());
        builderMessage.setHour(String.valueOf(Calendar.HOUR));
        builderMessage.setMinute(String.valueOf(Calendar.MINUTE));
        builderMessage.setGroup(groupName);

        MessageProto.Message messageToSend = builderMessage.build();

        byte[] buffer = messageToSend.toByteArray();

        channel.basicPublish(groupName, currentUser, null,  buffer);
      }
      
      // Enviar mensagem para a fila do usuário
      if(message.charAt(0) == '@'){
        // Iniciar chat com um usuário
        currentChatCommand = message+">>";
        currentUser = message.substring(1);
        isGroup = false;
      }else if(message.equals("!exit")){
        // Sair do chat
        break;
      }else if(message.contains("!addGroup")){
        // Criar grupo
        String groupName = message.split(" ")[1];
        channel.exchangeDeclare(groupName, "fanout");
      }else if (message.contains("!addUser")){
        // Adicionar usuário ao grupo
        String groupName = message.split(" ")[1];
        String userToAdd = message.split(" ")[2];
        channel.queueBind(userToAdd, groupName, "");
      }else if (message.contains("#") && !isGroup){
        // Iniciar chat com um grupo
        currentChatCommand = message+">>";
        isGroup = true;
      }
      else{
        // Enviar mensagem para a fila do usuário
        Date  date= new Date(System.currentTimeMillis());
    
        Calendar.getInstance().setTime(date);
        MessageProto.Message.Builder builderMessage = MessageProto.Message.newBuilder();
        builderMessage.setSenderName(user);
        builderMessage.setMessage(message);
        builderMessage.setDate(date.toString());
        builderMessage.setHour(String.valueOf(Calendar.HOUR));
        builderMessage.setMinute(String.valueOf(Calendar.MINUTE));
        builderMessage.setGroup(null);
        
        MessageProto.Message messageToSend = builderMessage.build();

        byte[] buffer = messageToSend.toByteArray();
        channel.basicPublish("", currentUser, null,  buffer);
      }
    }
  }
}