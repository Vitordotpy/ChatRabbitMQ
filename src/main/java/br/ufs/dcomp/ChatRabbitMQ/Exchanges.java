package br.ufs.dcomp.ChatRabbitMQ;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Exchanges {

  private final static String QUEUE_NAME = "minha-fila";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("34.224.118.154"); // Alterar
    factory.setUsername("admin"); // Alterar
    factory.setPassword("admin"); // Alterar
    factory.setVirtualHost("/");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    
    channel.exchangeDeclare("E1", "fanout");
    channel.exchangeDeclare("E2", "direct");
    
    String currentRoute = "A";
    
    int n = 10;
    for (int i = 0; i < n ; i++){
      if(i%2 == 0){
        channel.queueBind(QUEUE_NAME+String.valueOf(i), "E2", currentRoute);
        
        System.out.println("Exchange E2: associado a fila " + QUEUE_NAME+String.valueOf(i) + " Com a rota" + currentRoute);
        
        if(currentRoute == "A"){
          currentRoute = "B";
        }else{
          currentRoute = "A";
        }
      }else{
        channel.queueBind(QUEUE_NAME+String.valueOf(i), "E1", "");
        System.out.println("Exchange E1: associado a fila " + QUEUE_NAME+String.valueOf(i) + " sem a rota");
      }
    }
    //(queue-name, durable, exclusive, auto-delete, params); 
    
    //
    //

    channel.close();
    connection.close();
  }
}