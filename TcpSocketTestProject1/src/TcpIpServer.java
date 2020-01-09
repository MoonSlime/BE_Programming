import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class TcpIpServer { 

	private static final Logger LOG = Logger.getLogger(TcpIpServer.class.getName());
	
	public static void main(String[] args) {
		
		try(ServerSocket serverSocket = new ServerSocket(8080)){
            while(true) {
                // 요청이 들어올때까지 대기, 클라이언트의 연결요청이 들어오면 클라이언트소켓과 통신할 소켓을 반환
                Socket socket = serverSocket.accept(); //listen.
                LOG.info(socket.getInetAddress() +"로부터 연결요청이 들어왔습니다.");
                
                // 출력스트림 
                OutputStream out = socket.getOutputStream();
                DataOutputStream dos = new DataOutputStream(out);
                
                dos.writeUTF("Message from server");
                LOG.info("메시지 전송 완료");
                
                dos.close();         
            }
        }catch(IOException e) {
        	LOG.severe(e.toString());
        }
		
		//System.out.println(LOG.toString());
	}
}
