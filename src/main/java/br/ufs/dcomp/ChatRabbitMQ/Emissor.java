package br.ufs.dcomp.ChatRabbitMQ;

import java.util.Scanner;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Emissor {


  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("34.224.118.154"); // Alterar
    factory.setUsername("admin"); // Alterar
    factory.setPassword("admin"); // Alterar
    factory.setVirtualHost("/");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    
    String message = "Not Blank";
    Scanner sc = new Scanner(System.in);
    while (!message.isBlank()){
      message = sc.nextLine();
      channel.basicPublish("E1", " ", null,  message.getBytes("UTF-8"));
      System.out.println(" [x] Mensagem enviada: '" + message + "'");
    }
    sc.close();
    channel.close();
    connection.close();
  }
}