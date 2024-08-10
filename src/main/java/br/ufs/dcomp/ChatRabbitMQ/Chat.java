package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.*;

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
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

        // Realizar tratamento do objeto da mensagem como buffer de bytes
        String message = new String(body, "UTF-8");

        System.out.println(message);

      }
    };
    //(queue-name, autoAck, consumer);    
    channel.basicConsume(user, true,    consumer);

    String currentChatCommand = ">>";
    String currentUser = "";
    // Chat ativo
    while (true) {
      System.out.print(currentChatCommand);
      String message = sc.nextLine();
      
      // Enviar mensagem para a fila do usuário
      if(message.charAt(0) == '@'){
        currentChatCommand = message+">>";
        currentUser = message.substring(1);
      }else if(message.equals("!exit")){
        break;
      }
      else{
        Date  date= new Date(System.currentTimeMillis());
    
        Calendar.getInstance().setTime(date);

        channel.basicPublish("", currentUser, null,  ("("+date.toString()+" às "+ Calendar.HOUR + ":" + Calendar.MINUTE + ")" + user + "diz: " + message).getBytes("UTF-8"));
      }
    }
  }
}