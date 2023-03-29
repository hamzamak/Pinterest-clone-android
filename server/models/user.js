import mongoose from "mongoose";

const userSchema = new mongoose.Schema({
    email : {type : String , required : true},
    username : {type : String , required : true},
    password : {type : String , required : true},
    avatarUrl : {type : String }

})

export default mongoose.model('User',userSchema )
 