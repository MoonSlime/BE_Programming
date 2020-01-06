import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class TcpIpWebServer {
//	private static final String DEFAULT_FILE_PATH = "/Users/user/Documents/GitHub/BE_Programming/TcpSocketTestProject1/src/index.html";
	private static final String DEFAULT_FILE_PATH = TcpIpWebServer.class.getResource("").getPath() + "/index.html";
	private static final Logger LOG = Logger.getLogger(TcpIpWebServer.class.getName());

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(8080)) {

			while (true) {
				// 요청이 들어올때까지 대기, 클라이언트의 연결요청이 들어오면 클라이언트소켓과 통신할 소켓을 반환
				Socket socket = serverSocket.accept();
				LOG.info(socket.getInetAddress() + "로부터 연결요청이 들어왔습니다.");

				// 클라이언트 통신을 위한 입출력 스트림 생성
				BufferedReader inFromClient = null;
				DataOutputStream outToClient = null;

				// 입력스트림 초기화
				InputStream in = socket.getInputStream();
				inFromClient = new BufferedReader(new InputStreamReader(in));

				// 출력스트림 초기화
				OutputStream out = socket.getOutputStream();
				outToClient = new DataOutputStream(out);
				LOG.info("스트림 초기화 완료");

				String fileName = DEFAULT_FILE_PATH;
				File file = new File(fileName);
				if (file.exists()) {
					// 존재하는 파일의 MIME타입을 분석한다.
					String mimeType = URLConnection.guessContentTypeFromName(fileName);
					LOG.info("MIME TYPE : " + mimeType);

					// 파일의 바이트수를 찾아온다.
					int numOfBytes = (int) file.length();

					// 파일을 스트림을 읽어들일 준비를 한다.
					FileInputStream inFile = new FileInputStream(fileName);
					byte[] fileInBytes = new byte[numOfBytes];
					inFile.read(fileInBytes);

					// 정상적으로 처리가 되었음을 나타내는 200 코드를 출력한다.
					outToClient.writeBytes("HTTP/1.0 200 Document Follows \r\n");
					outToClient.writeBytes("Content-Type: " + mimeType + "\r\n");

					// 출력할 컨텐츠의 길이를 출력
					outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");
					outToClient.writeBytes("\r\n");

					// 요청 파일을 출력한다.
					outToClient.write(fileInBytes, 0, numOfBytes);
					LOG.info("파일 전송 완료");
				}

				in.close();
				out.close();
				socket.close();
			}
		} catch (IOException e) {
			LOG.severe(e.toString());
		}
	}

}
