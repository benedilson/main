package org.main;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Main implements Runnable {

	static String sistemaOperacional;
	static String ip;
	static String mac;
	static String nomeDaMaquina;
	static String nomeCompletoDaMaquina;
	static String envio;
	private static ServerSocket servidor;

	private final static String getMacAddress() throws IOException {
		String os = System.getProperty("os.name");
		if (os.startsWith("Windows")) {
			return windowsRunIpConfigCommand();
		} else if (os.startsWith("Linux")) {
			return linuxRunIfConfigCommand();
		} else {
			throw new IOException("Sistema operacional desconhecido: " + os);
		}
	}

	private final static String linuxRunIfConfigCommand() throws IOException {
		Process p = Runtime.getRuntime().exec("ifconfig");
		InputStream stdoutStream = new BufferedInputStream(p.getInputStream());
		StringBuffer buffer = new StringBuffer();
		for (;;) {
			int c = stdoutStream.read();
			if (c == -1)
				break;
			buffer.append((char) c);
		}
		String outputText = buffer.toString();
		int index = outputText.indexOf("inet end.:");
		String mac = outputText.substring(46, index);
		stdoutStream.close();
		return mac.trim();
	}

	private final static String windowsRunIpConfigCommand() throws IOException {
		Process p = Runtime.getRuntime().exec("getmac /v /fo list");
		InputStream stdoutStream = new BufferedInputStream(p.getInputStream());
		StringBuffer buffer = new StringBuffer();
		for (;;) {
			int c = stdoutStream.read();
			if (c == -1)
				break;
			buffer.append((char) c);
		}
		String outputText = buffer.toString();
		int index = outputText.indexOf("Nome de transporte:");
		String mac = outputText.substring(index - 19, index);
		stdoutStream.close();
		return mac.trim();
	}

	public static void main(String[] args) {
		try {
			servidor = new ServerSocket(50958);

			while (true) {

				Socket socket = servidor.accept();
				//Cria um objeto que vai tratar a conexão
				Main m = new Main();
				
				Thread t = new Thread(m);
				
				t.start();
				
				System.out.println("Aceitou na porta -> " + socket.getPort());

				try {
					sistemaOperacional = System.getProperty("os.name");
					ip = InetAddress.getLocalHost().getHostAddress();
					mac = getMacAddress();
					nomeDaMaquina = InetAddress.getLocalHost().getHostName();
					nomeCompletoDaMaquina = InetAddress.getLocalHost().getCanonicalHostName();

					envio = "#" + mac + "#M" + nomeDaMaquina + "#";

				} catch (Throwable h) {
					h.printStackTrace();
				}

				PrintStream saida = new PrintStream(socket.getOutputStream());
				saida.println(envio);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		for (int i = 0; i < 2; i++) {
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
		
	}
}
