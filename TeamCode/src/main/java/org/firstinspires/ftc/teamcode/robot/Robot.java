package org.firstinspires.ftc.teamcode.robot;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.drive.MecanumDrive;
import org.firstinspires.ftc.teamcode.drive.TankDrive;

import java.util.ArrayList;
import java.util.HashMap;

public class Robot
{
    HashMap<ArmPosition.Horizontal, Double> horizontalArmPositions =
            new HashMap<ArmPosition.Horizontal, Double>();
    HashMap<ArmPosition.Vertical, Double> verticalArmPositions =
            new HashMap<ArmPosition.Vertical, Double>();

    public  double horizontalClawOpenPosition = 0.5;
    public  double horizontalClawClosePosition = 0;
    public  double verticalClawOpenPosition = 0.5;
    public  double verticalClawClosePosition = 0;

    private boolean isHorizontalClawOpen = true;
    private boolean isVerticalClawOpen = true;

    private ArrayList<DcMotor> horizontalSlideMotors = new ArrayList<DcMotor>();
    private ArrayList<DcMotor> verticalSlideMotors = new ArrayList<DcMotor>();

    private Servo verticalArmServo;
    private Servo horizontalArmServo;

    private Servo horizontalClawServo;
    private Servo verticalClawServo;

    private ArmPosition.Vertical currentVerticalArmPosition =
            ArmPosition.Vertical.CONNECTING_POSITION;
    private ArmPosition.Horizontal currentHorizontalArmPosition =
            ArmPosition.Horizontal.CONNECTING_POSITION;

    private String[] verticalSlideMotorNames;
    private String[] horizontalSlideMotorNames;

    public MecanumDrive drive = new MecanumDrive();

    public Robot(HashMap<ArmPosition.Horizontal, Double> horizontalPosition,
                 HashMap<ArmPosition.Vertical, Double> verticalPosition,
                 String[] verticalSlideMotorNames, String[] horizontalSlideMotorNames)
    {
        horizontalArmPositions = horizontalPosition;
        verticalArmPositions = verticalPosition;
        this.verticalSlideMotorNames = verticalSlideMotorNames;
        this.horizontalSlideMotorNames = horizontalSlideMotorNames;
    }
    public void init(@NonNull HardwareMap hardwareMap)
    {
        for (String name : verticalSlideMotorNames)
        {
            verticalSlideMotors.add(hardwareMap.dcMotor.get(name));
        }
        for (String name : horizontalSlideMotorNames)
        {
            horizontalSlideMotors.add(hardwareMap.dcMotor.get(name));
        }

        for (DcMotor slideMotor : horizontalSlideMotors)
        {
            slideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
        for (DcMotor slideMotor : verticalSlideMotors)
        {
            slideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            slideMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        }
        verticalSlideMotors.get(0).setDirection(DcMotorSimple.Direction.REVERSE);

        verticalArmServo = hardwareMap.servo.get("verticalArm");
        horizontalArmServo = hardwareMap.servo.get("horizontalArm");

        horizontalClawServo = hardwareMap.servo.get("horizontalClaw");
        verticalClawServo = hardwareMap.servo.get("verticalClaw");

        drive.init(hardwareMap);
    }
    public void SetSlidesMotorPower(@NonNull Slide slide, double power)
    {
        switch (slide)
        {
            case VERTICAL:
                for (DcMotor motor : verticalSlideMotors)
                {
                    motor.setPower(power);
                }
                break;
            case HORIZONTAL:
                for (DcMotor motor : horizontalSlideMotors)
                {
                    motor.setPower(power);
                }
                break;
        }
    }
    public double GetSlidesPosition(@NonNull Slide slide, int index)
    {
        switch (slide)
        {
            case VERTICAL:
                return verticalSlideMotors.get(index).getCurrentPosition();
            case HORIZONTAL:
                return horizontalSlideMotors.get(index).getCurrentPosition();
        }
        return 0;
    }
    public void SetArmPosition(ArmPosition.Horizontal position)
    {
        if (position == null) return;
        currentHorizontalArmPosition = position;
        horizontalArmServo.setPosition(horizontalArmPositions.get(currentHorizontalArmPosition));
    }
    public void SetArmPosition(ArmPosition.Vertical position)
    {
        if (position == null) return;
        currentVerticalArmPosition = position;
        verticalArmServo.setPosition(verticalArmPositions.get(currentVerticalArmPosition));
    }
    public double GetArmPosition(@NonNull Arm arm)
    {
        switch (arm)
        {
            case HORIZONTAL:
                return horizontalArmPositions.get(currentHorizontalArmPosition);
            case VERTICAL:
                return verticalArmPositions.get(currentVerticalArmPosition);
        }
        return 0;
    }
    public ArmPosition.Vertical GetVerticalArmPosition()
    {
        return currentVerticalArmPosition;
    }
    public ArmPosition.Horizontal GetHorizontalArmPosition()
    {
        return currentHorizontalArmPosition;
    }
    public void SetClawPosition(@NonNull Claw selectedClaw, boolean isOpen)
    {
        double position;
        switch (selectedClaw) {
            case HORIZONTAL:
                position = isOpen ? horizontalClawOpenPosition : horizontalClawClosePosition;
                horizontalClawServo.setPosition(position);
                isHorizontalClawOpen = isOpen;
                break;
            case VERTICAL:
                position = isOpen ? verticalClawOpenPosition : verticalClawClosePosition;
                verticalClawServo.setPosition(position);
                isVerticalClawOpen = isOpen;
                break;
        }
    }
    public boolean IsThisClawOpen(@NonNull Claw referenceClaw)
    {
        switch (referenceClaw)
        {
            case HORIZONTAL:
                return isHorizontalClawOpen;
            case VERTICAL:
                return isVerticalClawOpen;
        }
        return false;
    }
    public double GetClawPosition(@NonNull Claw referenceClaw)
    {
        switch (referenceClaw)
        {
            case VERTICAL:
                return verticalClawServo.getPosition();
            case HORIZONTAL:
                return horizontalClawServo.getPosition();
        }
        return 0;
    }
}