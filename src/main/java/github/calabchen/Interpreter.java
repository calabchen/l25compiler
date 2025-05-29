package github.calabchen;


enum Fct {
	LIT, OPR, LOD, STO, CAL, INT, JMP, JPC
}


class Instruction {
	
	public Fct f;
	
	
	public int l;
	
	
	public int a;
}


public class Interpreter {

	final int stacksize = 500;
	

	public int cx = 0;
	

	public Instruction[] code = new Instruction[L25.cxmax];
	

	public void gen(Fct x, int y, int z) {
		if (cx >= L25.cxmax) {
			throw new Error("Program too long");
		}
		
		code[cx] = new Instruction();
		code[cx].f = x;
		code[cx].l = y;
		code[cx].a = z;
		cx ++;
	}


	public void listcode(int start) {
		if (L25.listswitch) {
			for (int i=start; i<cx; i++) {
				String msg = i + " " + code[i].f + " " + code[i].l + " " + code[i].a;
				System.out.println(msg);
				L25.fa.println(msg);
			}
		}
	}
	

	public void interpret() {
		int p, b, t;
		Instruction i;
		int[] s = new int[stacksize];
		
		System.out.println("start pl0");
		t = b = p = 0;
		s[0] = s[1] = s[2] = 0;
		do {
			i = code[p];
			p ++;
			switch (i.f) {
			case LIT:
				s[t] = i.a;
				t++;
				break;
			case OPR:
				switch (i.a)
				{
				case 0:
					t = b;
					p = s[t+2];
					b = s[t+1];
					break;
				case 1:
					s[t-1] = -s[t-1];
					break;
				case 2:
					t--;
					s[t-1] = s[t-1]+s[t];
					break;
				case 3:
					t--;
					s[t-1] = s[t-1]-s[t];
					break;
				case 4:
					t--;
					s[t-1] = s[t-1]*s[t];
					break;
				case 5:
					t--;
					s[t-1] = s[t-1]/s[t];
					break;
				case 6:
					s[t-1] = s[t-1]%2;
					break;
				case 8:
					t--;
					s[t-1] = (s[t-1] == s[t] ? 1 : 0);
					break;
				case 9:
					t--;
					s[t-1] = (s[t-1] != s[t] ? 1 : 0);
					break;
				case 10:
					t--;
					s[t-1] = (s[t-1] < s[t] ? 1 : 0);
					break;
				case 11:
					t--;
					s[t-1] = (s[t-1] >= s[t] ? 1 : 0);
					break;
				case 12:
					t--;
					s[t-1] = (s[t-1] > s[t] ? 1 : 0);
					break;
				case 13:
					t--;
					s[t-1] = (s[t-1] <= s[t] ? 1 : 0);
					break;
				case 14:
					System.out.print(s[t-1]);
					L25.fa2.print(s[t-1]);
					t--;
					break;
				case 15:
					System.out.println();
					L25.fa2.println();
					break;
				case 16:
					System.out.print("?");
					L25.fa2.print("?");
					s[t] = 0;
					try {
						s[t] = Integer.parseInt(L25.stdin.readLine());
					} catch (Exception e) {}
					L25.fa2.println(s[t]);
					t++;
					break;
				}
				break;
			case LOD:
				s[t] = s[base(i.l,s,b)+i.a];
				t++;
				break;
			case STO:
				t--;
				s[base(i.l, s, b) + i.a] = s[t];
				break;
			case CAL:
				s[t] = base(i.l, s, b);
				s[t+1] = b;
				s[t+2] = p;
				b = t;
				p = i.a;
				break;
			case INT:
				t += i.a;
				break;
			case JMP:
				p = i.a;
				break;
			case JPC:
				t--;
				if (s[t] == 0)
					p = i.a;
				break;
			}
		} while (p != 0);
	}
	

	private int base(int l, int[] s, int b) {
		int b1 = b;
		while (l > 0) {
			b1 = s[b1];
			l --;
		}
		return b1;
	}
}
