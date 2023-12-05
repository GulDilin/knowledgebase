public final class Main {
    /**
     * Default empty constructor.
     */
    private Main() {
        // empty constructor
    }

    /**
     * Main method for standalone app.
     *
     * @param argv CLI args
     */
    public static void main(final String[] argv) {
        Weld initializer = new Weld();
        // Initialize CDI container
        WeldContainer container = initializer.initialize();
        // Select bean from container. All injected dependencies will be initialized automatically.
        ExampleService service = container.select(ExampleService.class).get();
        // Invoke method from selected service
        System.out.println(service.computeAnotherValue()); // Prints 4
    }
}
