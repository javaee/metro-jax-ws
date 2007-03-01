package fromwsdl_wsaddressing.server;

import javax.jws.WebService;

@WebService(endpointInterface = "fromwsdl_wsaddressing.server.AddNumbersPortType")
public class AddNumbersImpl implements AddNumbersPortType {


    public int addNumbers(int number1, int number2)
            throws AddNumbersFault_Exception {
        return impl(number1, number2);
    }

    public int addNumbers2(int number1, int number2)
            throws AddNumbersFault_Exception {
        return impl(number1, number2);
    }

    public int addNumbers3(int number1, int number2)
            throws AddNumbersFault_Exception {
        return impl(number1, number2);
    }

    int impl(int number1, int number2) throws AddNumbersFault_Exception {
        if (number1 < 0 || number2 < 0) {
            AddNumbersFault fb = new AddNumbersFault();
            fb.setDetail("Negative numbers cant be added!");
            fb.setMessage("Numbers: " + number1 + ", " + number2);

            throw new AddNumbersFault_Exception(fb.getMessage(), fb);
        }

        return number1 + number2;
    }
}
