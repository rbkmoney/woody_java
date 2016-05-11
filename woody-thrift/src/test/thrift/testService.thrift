namespace java com.rbkmoney.woody.rpc

typedef i32 int // We can use typedef to get pretty names for the types we are using
struct Owner {
1:int id,
2:string name
}

exception err_one {
1:int id
}

service OwnerService
{
        Owner getOwner(1:int id),
        Owner getErrOwner(1:int id) throws (1:err_one err),
        void setOwner(1:Owner owner),
        oneway void setOwnerOneway(1:Owner owner)
        Owner setErrOwner(1:Owner owner, 2:int id) throws (1:err_one err)
}