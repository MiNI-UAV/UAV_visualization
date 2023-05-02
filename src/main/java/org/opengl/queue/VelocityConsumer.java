//package org.opengl.queue;
//
//import org.opengl.model.DroneStatus;
//import org.opengl.parser.MessageParserDroneStatus;
//import org.zeromq.ZContext;
//import org.zeromq.ZMQ;
//
//public class VelocityConsumer {
//    private DroneStatus droneStatus;
//
//    private ZContext context;
//    private ZMQ.Socket socket;
//    private MessageParserDroneStatus messageParser;
//
//
//    public VelocityConsumer(DroneStatus droneStatus) {
//        this.droneStatus = droneStatus;
//        messageParser = new MessageParserDroneStatus();
//        context = new ZContext();
//        socket = context. createSocket(ZMQ.SUB);
//        socket.connect("tcp://192.168.234.1:5556");
//        socket.subscribe("pos:".getBytes());
//        var thread = new VelocityConsumer.PositionThread();
//        thread.start();
//    }
//
//    class PositionThread extends Thread {
//        public void run() {
//            while (!Thread.currentThread().isInterrupted()) {
//                byte[] reply = socket.recv(0);
//                String message = new String(reply);
//                System.out.println("Received: [" + message + "]");
//                //String response = "Hello, world!";
//                //socket.send(response.getBytes(ZMQ.CHARSET), 0);
//                var newStatus = messageParser.parse(message);
//                droneStatus.position = newStatus.position;
//                droneStatus.rotation = newStatus.rotation;
//            }
//        }
//    }
//}
