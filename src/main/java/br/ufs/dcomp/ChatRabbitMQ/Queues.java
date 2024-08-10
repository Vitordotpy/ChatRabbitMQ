package br.ufs.dcomp.ChatRabbitMQ;

import java.util.Scanner;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Queues {

  private final static String QUEUE_NAME = "minha-fila";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("34.224.118.154"); // Alterar
    factory.setUsername("admin"); // Alterar
    factory.setPassword("admin"); // Alterar
    factory.setVirtualHost("/");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    
    int n = 10;
    for (int i = 0; i < n ; i++){
      channel.queueDeclare(QUEUE_NAME+String.valueOf(i), false,   false,     false,       null);
      channel.basicPublish("", QUEUE_NAME+String.valueOf(i), null,  ("Fila criada: "+QUEUE_NAME+String.valueOf(i)).getBytes("UTF-8"));
      System.out.println(" [x] Fila criada: '" + QUEUE_NAME+String.valueOf(i) + "'");
    }
    //(queue-name, durable, exclusive, auto-delete, params); 
    
    //
    //

    channel.close();
    connection.close();
  }
}