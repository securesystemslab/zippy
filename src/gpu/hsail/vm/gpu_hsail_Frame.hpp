#ifndef GPU_HSAIL_FRAME_HPP
#define GPU_HSAIL_FRAME_HPP

#include "graal/graalEnv.hpp"
#include "code/debugInfo.hpp"
#include "code/location.hpp"

class HSAILFrame {
  friend class VMStructs;
private:
  jint  _pc_offset;  // The HSAIL "pc_offset" where the exception happens
  jbyte _num_s_regs;
  jbyte _num_d_regs;
  jshort _num_stack_slots; 
  jbyte  _save_area[0];     // save area size can vary per kernel compilation  

public:
  // Accessors
  jint pc_offset() { return _pc_offset; }
  jint num_s_regs() {return _num_s_regs; }
  jint num_d_regs() {return _num_d_regs; }
  jint num_stack_slots() {return _num_stack_slots; }
  jlong get_d_reg(int idx) {
    int ofst = num_s_regs() * 4 + idx * 8;
    return(*(jlong *) (_save_area + ofst));
  }
  jint get_s_reg(int idx) {
    int ofst = idx * 4;
    return(*(jint *) (_save_area + ofst));
  }
  void put_d_reg(int idx, jlong val) {
    int ofst = num_s_regs() * 4 + idx * 8;
    (*(jlong *) (_save_area + ofst)) = val;
  }
  jint get_stackslot32(int stackOffset) {
    int ofst = num_s_regs() * 4 + num_d_regs() * 8 + stackOffset;
    return(*(jint *) (_save_area + ofst));
  }
  jlong get_stackslot64(int stackOffset) {
    int ofst = num_s_regs() * 4 + num_d_regs() * 8 + stackOffset;
    return(*(jlong *) (_save_area + ofst));
  }
  void put_stackslot64(int stackOffset, jlong val) {
    int ofst = num_s_regs() * 4 + num_d_regs() * 8 + stackOffset;
    (*(jlong *) (_save_area + ofst)) = val;
  }
};
  
#endif // GPU_HSAIL_FRAME_HPP
