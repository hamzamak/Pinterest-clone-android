import mongoose from "mongoose";

const postSchema = new mongoose.Schema({
    description : {type : String , default :"" },
    media : {type : String , required : true},
    likes : {type: [String] , default : []},
    owner : {type : mongoose.Schema.Types.ObjectId , ref : "User" ,required : true },
    createdAt : {type : Date , default : new Date()}   ,
    comments : {type : [mongoose.Schema.Types.ObjectId] ,ref : 'Comment' }


})

export default mongoose.model('Post',postSchema )
