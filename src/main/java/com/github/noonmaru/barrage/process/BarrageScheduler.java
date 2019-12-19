package com.github.noonmaru.barrage.process;

import com.github.noonmaru.tap.packet.Packet;

/**
 * @author Nemo
 */
public class BarrageScheduler implements Runnable
{
    private final BarrageProcess process;

    private Task task;

    public BarrageScheduler(BarrageProcess process)
    {
        this.process = process;
        this.task = new TitleTask();
    }

    @Override
    public void run()
    {
        task = task.execute();
    }

    private interface Task
    {
        Task execute();
    }

    private class TitleTask implements Task
    {
        int ticks;

        @Override
        public Task execute()
        {
            if (ticks++ == 0)
            {
                Packet.TITLE.compound("탄막 피하기", "", 5, 50, 5).sendAll();
            }

            if (ticks >= 60)
            {
                return new GameTask();
            }

            return this;
        }
    }

    private class GameTask implements Task
    {
        @Override
        public Task execute()
        {
            for (BarragePlayer player : process.getOnlinePlayers())
            {
                player.onUpdate();
            }

            for (Bullet bullet : process.getBullets())
            {
                bullet.onUpdate();
            }

            process.getBullets().removeIf(Bullet::isDead);

            if (process.getSurvivors().isEmpty())
            {
                process.stop();
                Packet.TITLE.compound("게임종료!", "", 0, 60, 10).sendAll();
            }

            return this;
        }
    }
}
