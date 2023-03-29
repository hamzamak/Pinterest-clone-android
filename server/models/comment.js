import mongoose from "mongoose";

const CommentSchema = mongoose.Schema({
    content : { type : String , required : true} ,
    owner : { type : mongoose.Schema.Types.ObjectId ,ref : 'User', required : true} ,
    createdAt : {type : Date , default : new Date()}  
})


export default mongoose.model('Comment',CommentSchema )