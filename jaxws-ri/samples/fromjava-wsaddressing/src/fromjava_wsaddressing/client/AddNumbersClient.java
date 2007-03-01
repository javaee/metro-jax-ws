package fromjava_wsaddressing.client;

public class AddNumbersClient {
    int number1 = 10;
    int number2 = 10;
    int negativeNumber = -10;

    public static void main(String[] args) {
        AddNumbersClient client = new AddNumbersClient();
        client.addNumbers();
        client.addNumbersFault();
        client.addNumbers2();
        client.addNumbers3Fault();
    }

    public void addNumbers() {
        System.out.printf("addNumbers: basic input and output name mapping\n");
        try {
            AddNumbersImpl stub = createStub();
            int result = stub.addNumbers(number1, number2);
            assert result == 20;
        } catch (Exception ex) {
            ex.printStackTrace();
            assert false;
        }
        System.out.printf("\n\n");
    }

    public void addNumbersFault() {
        System.out.printf("addNumbersFault: fault caused by out of bounds parameter value\n");
        AddNumbersImpl stub;

        try {
            stub = createStub();
            stub.addNumbers(negativeNumber, number2);
            assert false;
        } catch (AddNumbersException_Exception ex) {
            //This is expected exception
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
        System.out.printf("\n\n");
    }

    public void addNumbers2() {
        System.out.printf("addNumbers2: default input and output name mapping\n");
        try {
            AddNumbersImpl stub = createStub();
            int result = stub.addNumbers2(number1, number2);
            assert result == 20;
        } catch (Exception ex) {
            ex.printStackTrace();
            assert false;
        }
        System.out.printf("\n\n");
    }

    public void addNumbers3Fault() {
        System.out.printf("addNumbers3Fault: custom fault mapping\n");
        try {
            createStub().addNumbers3(negativeNumber, number2);
            assert false;
        } catch (AddNumbersException_Exception e) {
            //This is expected exception
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
        System.out.printf("\n\n");
    }

    private AddNumbersImpl createStub() throws Exception {
        return new AddNumbersImplService().getAddNumbersImplPort();
    }
}
