package com.navercorp.chat.dao;

import org.springframework.stereotype.Component;

@Component
public class testDao {

	private int n = 0;

	public testDao() {
		System.out.println("testDao 생성");
	}

	public synchronized void exec() {
		System.out.println("testDao 사용" + n);

		int sleepCnt = 0;
		while (sleepCnt < 5) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sleepCnt++;
			System.out.println("testDao"+n + "sleepCnt" + sleepCnt);
		}
		System.out.println("testDao 반환" + n++);
	}
}
