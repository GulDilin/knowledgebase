/**
 * This class describes an example service. It is Bean.
 */
@ApplicationScoped
public class ExampleService {

    /**
     * Example repository that will be injected.
     */
    @Inject
    private ExampleRepository repository;

    /**
     * Calculates the another value.
     *
     * @return     Another value.
     */
    public Integer computeAnotherValue() {
        this.repository.computeSomeValue() + 2;
    }
}
