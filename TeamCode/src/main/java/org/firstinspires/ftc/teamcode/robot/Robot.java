package org.firstinspires.ftc.teamcode.robot;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.drive.TankDrive;

import java.util.HashMap;

public class Robot
{
    HashMap<ArmPosition.Horizontal, Double> horizontalArmPositions =
            new HashMap<ArmPosition.Horizontal, Double>();
    HashMap<ArmPosition.Vertical, Double> verticalArmPositions =
            new HashMap<ArmPosition.Vertical, Double>();

    public  double horizontalClawOpenPosition = 0.75;
    public  double horizontalClawClosePosition = 0;
    public  double verticalClawOpenPosition = 0.5;
    public  double verticalClawClosePosition = 0;

    private boolean isHorizontalClawOpen = true;
    private boolean isVerticalClawOpen = true;

    private DcMotor horizontalSlideMotor;
    private DcMotor verticalSlideMotor;

    private Servo verticalArmServo;
    private Servo horizontalArmServo;

    private Servo horizontalClawServo;
    private Servo verticalClawServo;

    private ArmPosition.Vertical currentVerticalArmPosition =
            ArmPosition.Vertical.CONNECTING_POSITION;
    private ArmPosition.Horizontal currentHorizontalArmPosition =
            ArmPosition.Horizontal.CONNECTING_POSITION;

    public TankDrive drive = new TankDrive();

    public Robot(HashMap<ArmPosition.Horizontal, Double> horizontalPosition,
                 HashMap<ArmPosition.Vertical, Double> verticalPosition)
    {
        horizontalArmPositions = horizontalPosition;
        verticalArmPositions = verticalPosition;
    }
    public void init(@NonNull HardwareMap hardwareMap)
    {
        horizontalSlideMotor = hardwareMap.dcMotor.get("horizontalSlide");
        verticalSlideMotor = hardwareMap.dcMotor.get("verticalSlide");

        horizontalSlideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        verticalSlideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        verticalSlideMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

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
                verticalSlideMotor.setPower(power);
                break;
            case HORIZONTAL:
                horizontalSlideMotor.setPower(power);
                break;
        }
    }
    public double GetSlidesPosition(@NonNull Slide slide)
    {
        switch (slide)
        {
            case VERTICAL:
                return verticalSlideMotor.getCurrentPosition();
            case HORIZONTAL:
                return horizontalSlideMotor.getCurrentPosition();
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
    public boolean GetClawPosition(@NonNull Claw referenceClaw)
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
    public boolean IsThisClawOpen(@NonNull Claw referenceClaw)
    {
        switch (referenceClaw)
        {
            case VERTICAL:
                return isVerticalClawOpen;
            case HORIZONTAL:
                return isHorizontalClawOpen;
        }
        return false;
    }
}