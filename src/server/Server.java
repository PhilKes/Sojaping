package server;

import clientLogic.Client;

import java.util.List;

public class Server {

	private TranslationService translationService;

	private List<Client> clients;

	public static void main(String[] args) {
		System.out.println("Hi from server.");
	}
}
