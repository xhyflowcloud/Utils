package HorseRace;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

class Horse implements Runnable{
    private static int counter = 0;
    private final int id = counter++;
    private int strides = 0;
    private static Random rand = new Random(47);
    private static CyclicBarrier barrier;

    public Horse(CyclicBarrier b){
        barrier = b;
    }

    public synchronized int getStrides(){
        return strides;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()){
            synchronized (this){
                strides += rand.nextInt(3);
            }
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }
    public String toString(){
        return "Horse " + id + " ";
    }
    public String tracks(){
        StringBuffer s = new StringBuffer();
        for(int i = 0; i < getStrides(); i++)
            s.append("*");
        s.append(id);
        return s.toString();
    }
}

public class HorseRace {
    static final int FINISH_LINE = 75;
    private List<Horse> horses = new ArrayList<>();
    private ExecutorService exec = Executors.newCachedThreadPool();
    private CyclicBarrier barrier;
    public HorseRace(int nHorse, final int pause){
        barrier = new CyclicBarrier(nHorse, new Runnable() {
            @Override
            public void run() {
                StringBuilder s = new StringBuilder();
                for(int i = 0; i < FINISH_LINE; i++){
                    s.append("=");
                }
                System.out.println(s);
                for(Horse horse: horses){
                    System.out.println(horse.tracks());
                }
                for(Horse horse: horses){
                    if(horse.getStrides() >= FINISH_LINE){
                        System.out.println(horse + "Won! ");
                        exec.shutdownNow();
                    }
                    return;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(pause);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        for(int i = 0; i< nHorse; i++){
            Horse horse = new Horse(barrier);
            horses.add(horse);
            exec.execute(horse);
        }
    }

    public static void main(String[] args) {
        int nHorse = 7;
        int pause = 200;
        new HorseRace(nHorse, pause);
    }
}
