class RentalRecodeLinkedList {
    RentalRecode first;

    public RentalRecodeLinkedList() {
        this.first = null;
    }

    public boolean isEmpty() {
        return first == null;
    }

    public void insertFirst(int id, String vehicle, String customer, String rentDate, String returnDate, double totalCost) {
        RentalRecode newLink = new RentalRecode(id, vehicle, customer, rentDate, returnDate, totalCost);
        newLink.next = first;
        first = newLink;
    }

    public void insertAfter(int key, int id, String vehicle, String customer, String rentDate, String returnDate, double totalCost) {
        RentalRecode current = first;
        while (current != null) {
            RentalRecode newLink = new RentalRecode(id, vehicle, customer, rentDate, returnDate, totalCost);
            newLink.next = current.next;
            current.next = newLink;
            return;
        }
        current = current.next;
    }

    public RentalRecode find(int key) {
        RentalRecode current = first;

        while (current != null) {
            if (current.recodeID == key) {
                return current;
            }
            current = current.next;
        }
        return null;
    }
    public RentalRecode DeleteFirst(){
        if(isEmpty()){
            System.out.println("Deletation faild");
            return null;
        }
        RentalRecode temp=first;
        first=first.next;
        return temp;
    }
    public void Delete(int key){
        if(isEmpty()){
            System.out.println("Deletation faild");
        }
        RentalRecode current=first;
        RentalRecode pervious=first;

        while(current!=null){
            if(current.recodeID==key){
                if(current==first){
                    first=first.next;
                }
                else{
                    pervious.next=current.next;
                }
                return;
            }
            pervious=current;
            current=current.next;
        }
    }
    public void DisplayList(){
        RentalRecode current=first;
        while(current!=null){
            current.displayLink();
            current=current.next;
        }
        System.out.println(" ");
    }
}


