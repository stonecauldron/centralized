package centralized;


/**
 * exception thrown when no solution exists for the given problem
 */
class NoSolutionException extends Throwable{

    private String err;

    public NoSolutionException(String err){
        this.err = err;
    }

    @Override
    public String toString(){
        return err;
    }

}
