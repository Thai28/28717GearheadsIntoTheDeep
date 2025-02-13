package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.drive.TankDrive;
import org.firstinspires.ftc.teamcode.robot.ArmPosition;
import org.firstinspires.ftc.teamcode.robot.Robot;

import java.util.Dictionary;
import java.util.Enumeration;

@TeleOp(name = "teleBot3")
public class teleBot3 extends OpMode
{
    Dictionary<ArmPosition.HorizontalPosition, Double> horizontalArmPosition =
            new Dictionary<ArmPosition.HorizontalPosition, Double>() {
                @Override
                public int size() {
                    return 0;
                }

                @Override
                public boolean isEmpty() {
                    return false;
                }

                @Override
                public Enumeration<ArmPosition.HorizontalPosition> keys() {
                    return null;
                }

                @Override
                public Enumeration<Double> elements() {
                    return null;
                }

                @Override
                public Double get(Object o) {
                    return 0.0;
                }

                @Override
                public Double put(ArmPosition.HorizontalPosition horizontalPosition, Double aDouble) {
                    return 0.0;
                }

                @Override
                public Double remove(Object o) {
                    return 0.0;
                }
            };
    Dictionary<ArmPosition.VerticalPosition, Double> verticalArmPosition =
            new Dictionary<ArmPosition.VerticalPosition, Double>() {
                @Override
                public int size() {
                    return 0;
                }

                @Override
                public boolean isEmpty() {
                    return false;
                }

                @Override
                public Enumeration<ArmPosition.VerticalPosition> keys() {
                    return null;
                }

                @Override
                public Enumeration<Double> elements() {
                    return null;
                }

                @Override
                public Double get(Object o) {
                    return 0.0;
                }

                @Override
                public Double put(ArmPosition.VerticalPosition verticalPosition, Double aDouble) {
                    return 0.0;
                }

                @Override
                public Double remove(Object o) {
                    return 0.0;
                }
            };

    Robot robot;
    @Override
    public void init()
    {
        horizontalArmPosition.put(ArmPosition.HorizontalPosition.CONNECTING_POSITION, 1.0);
        horizontalArmPosition.put(ArmPosition.HorizontalPosition.BARRIER_AVOIDING_POSITION, 0.45);
        horizontalArmPosition.put(ArmPosition.HorizontalPosition.AIMING_POSITION, 0.38);
        horizontalArmPosition.put(ArmPosition.HorizontalPosition.GRABBING_POSITION, 0.0);

        verticalArmPosition.put(ArmPosition.VerticalPosition.CONNECTING_POSITION, 0.0);
        verticalArmPosition.put(ArmPosition.VerticalPosition.DUMPING_POSITION, 0.85);
        verticalArmPosition.put(ArmPosition.VerticalPosition.SPECIMEN_POSITION, 1.0);

        robot = new Robot(horizontalArmPosition, verticalArmPosition);
        robot.init(hardwareMap);
    }

    @Override
    public void loop()
    {

    }
}
