import express  from "express";
import Post from "../models/post.js";
import  {v2 as cloudinary} from "cloudinary"
import auth from "../middleware/auth.js";
import mongoose from "mongoose";

cloudinary.config({
  cloud_name : process.env.CLOUDINARY_CLOUD_NAME,
  api_key : process.env.CLOUDINARY_CLOUD_API_KEY ,
  api_secret : process.env.CLOUDINARY_CLOUD_API_SECRET
})

const router = express.Router();

router.post('/create' ,auth, async (req , res ) => {
     const { media ,description } = req.body ;
     if (!req.userId) return res.status(401). json({ message: "User is not Authenticated" });

   try {
    req.setTimeout(2400000); //To cause requests to a particular action to time out after 4 minutes:
     const mediaUrl = await cloudinary.uploader.upload(media); //upload the image to the cloudinary
     const newPost = await Post.create({
       owner : req.userId ,
       media : mediaUrl.url ,
       description,
     })
     res.status(200).send(  newPost );   

   } catch (error) {
     return res.status(500).json({message:"error in creating A post"})
   }
 

})


router.get('/getAll' , async (req , res ) => {
    // const {owner , media ,description} = req.body ;

  try {
    const posts = await Post.find().sort({ _id: -1 }).populate({
      path: 'comments',
      populate: {
        path: 'owner',
        select: 'username avatarUrl' // you can specify the fields you want to retrieve from the owner model
      }
    });
    res.status(200).send(  posts );   

  } catch (error) {
    return res.status(500).json({message:"error in creating A post"})
  }


})

router.get('/get/:idUser' , async (req , res ) => {
   const {idUser} = req.params ;

try {
  const post = await Post.find({owner: idUser}).sort({ _id: -1 }).populate({
    path: 'comments',
    populate: {
      path: 'owner',
      select: 'username avatarUrl' 
    }
  });
 
  res.status(200).send(  post );   

} catch (error) {
  return res.status(500).json({message:"error in creating A post"})
}
})
 

router.post('/like/:id' ,auth, async (req , res ) => {
     const {id} = req.params ;
     if (!req.userId) return res.status(401). json({ message: "User is not Authenticated" });
     if (!mongoose.Types.ObjectId.isValid(id)) return res.status(404).send(`No post with id: ${id}`);
     const post = await Post.findById(id);
 
     const index = post.likes.findIndex((id) => id === String(req.userId))
     if (index === -1) {
        // like 
        post.likes.push(req.userId); 
     }
     else {
        //dislike
        post.likes = post.likes.filter((id) => id !== req.userId);
     }
  
     const updatedPost = await Post.findByIdAndUpdate(id, post, { new: true });
  
     res.json(updatedPost);
 

})


router.delete('/delete/:id',auth, async (req, res) => {
  const {id} = req.params ;
      try {
        if (!req.userId) return res.status(401). json( "User is not Authenticated" );
        const p = await Post.findById(id);
       
        if(String(p.owner) === String(req.userId)){   
          const post = await Post.findByIdAndDelete(id);
          return res.status(200).json(post);
        }

        return res.status(402).json( "user cannot deleted this post (only his own posts) ");
      } catch (error) {
        return res.status(500).json(error)
      }
})


router.put('/update',auth, async (req, res) => {
  const {_id,description} = req.body ;
      try {
    
        if (!req.userId) return res.status(401). json( "User is not Authenticated" );
        const p = await Post.findById(_id);
       
        if(String(p.owner) === String(req.userId)){   
          const post = await Post.findByIdAndUpdate(_id , {description} , {new : true});
          return res.status(200).json(post);
        }

        return res.status(402).json( "user cannot update this post (only his own posts) ");
      } catch (error) {
        return res.status(500).json(error)
      }
})



export default router ;